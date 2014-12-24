// Copyright (c) 2012-2014 K Team. All Rights Reserved.
package org.kframework.compile.transformers;

import org.kframework.compile.utils.GetLhsPattern;
import org.kframework.compile.utils.MetaK;
import org.kframework.kil.*;
import org.kframework.kil.Cell.Ellipses;
import org.kframework.kil.loader.Context;
import org.kframework.kil.visitors.BasicVisitor;
import org.kframework.utils.errorsystem.KException;
import org.kframework.utils.errorsystem.KException.ExceptionType;
import org.kframework.utils.errorsystem.KException.KExceptionGroup;
import org.kframework.utils.general.GlobalSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ResolveBlockingInput extends GetLhsPattern {
    
    Map<String, String> inputCells = new HashMap<String, String>();
    java.util.List<Rule> generated = new ArrayList<Rule>();
    boolean hasInputCell;
    Term resultCondition;
    boolean newList;

    public ResolveBlockingInput(Context context, boolean newList) {
        super("Resolve Blocking Input", context);
        this.newList = newList;
    }
    
    @Override
    public ASTNode visit(Definition node, Void _)  {
        Configuration config = MetaK.getConfiguration(node, context);
        new BasicVisitor(context) {
            @Override
            public Void visit(Cell node, Void _) {
                String stream = node.getCellAttributes().get("stream");
                if ("stdin".equals(stream)) {
                    String delimiter = node.getCellAttributes().get("delimiters");
                    if (delimiter == null) {
                        delimiter = " \n\t\r";
                    }
                    inputCells.put(node.getLabel(), delimiter);
                }
                return super.visit(node, _);
            }

        }.visitNode(config);
        return super.visit(node, _);
    }
    
    @Override
    public ASTNode visit(Module node, Void _)  {
        ASTNode result = super.visit(node, _);
        if (result != node) {
            GlobalSettings.kem.register(new KException(ExceptionType.ERROR, 
                    KExceptionGroup.INTERNAL, 
                    "Should have obtained the same module.", 
                    getName(), node.getFilename(), node.getLocation()));                    
        }
        if (generated.isEmpty()) return node;
        node = node.shallowCopy();
        node.getItems().addAll(generated);
        return node;
    }
    
    @Override
    public ASTNode visit(Configuration node, Void _)  {
        return node;
    }
    
    @Override
    public ASTNode visit(org.kframework.kil.Context node, Void _)  {
        return node;
    }
    
    @Override
    public ASTNode visit(Syntax node, Void _)  {
        return node;
    }
    
    @Override
    public ASTNode visit(Rule node, Void _)  {
        hasInputCell = false;
        if (node.getAttributes().containsKey("stdin")) {
            // a rule autogenerated by AddStreamCells, so we shouldn't touch it.
            return node;
        }
        ASTNode result = super.visit(node, _);
        if (hasInputCell) {
            generated.add((Rule)result);
            node.setRequires(MetaK.incrementCondition(node.getRequires(), resultCondition));
        }
        return node;
    }
    
    @Override
    public ASTNode visit(Cell node, Void _)  {
        if ((!inputCells.containsKey(node.getLabel()))) {
            return super.visit(node, _);
        }
        if (!(node.getEllipses() == Ellipses.RIGHT)) {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING, 
                    KExceptionGroup.COMPILER, 
                    "cell should have right ellipses but it doesn't." +
                            System.getProperty("line.separator") + "Won't transform.", 
                            getName(), node.getFilename(), node.getLocation()));
            return node;
        }
        Term contents = node.getContents();
        if (!(contents instanceof Rewrite)) {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING, 
                    KExceptionGroup.COMPILER, 
                    "Expecting a rewrite of a basic type variable into the empty list but got " + contents.getClass() + "." +
                            System.getProperty("line.separator") + "Won't transform.", 
                            getName(), contents.getFilename(), contents.getLocation()));
            return node;
        }
        Rewrite rewrite = (Rewrite) contents;
        if ((!newList && !(rewrite.getLeft() instanceof ListItem)) ||
            (newList && !(rewrite.getLeft() instanceof KApp &&
            ((KApp)rewrite.getLeft()).getLabel().equals(
                KLabelConstant.of(DataStructureSort.DEFAULT_LIST_ITEM_LABEL, context))))) {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING, 
                    KExceptionGroup.COMPILER, 
                    "Expecting a list item but got " + rewrite.getLeft().getClass() + "." +
                            System.getProperty("line.separator") + "Won't transform.", 
                            getName(), rewrite.getLeft().getFilename(), rewrite.getLeft().getLocation()));
            return node;            
        }
        Term item = rewrite.getLeft();
        Term variable;
        if (newList) {
            KApp kappItem = (KApp)item;
            Term child = kappItem.getChild();
            if (!(child instanceof KList) || ((KList)child).getContents().size() != 1) {
                GlobalSettings.kem.register(new KException(ExceptionType.WARNING, 
                    KExceptionGroup.COMPILER, 
                    "Expecting an input type variable but got a KList instead. Won't transform.", 
                            getName(), ((KApp)item).getChild().getFilename(), ((KApp)item).getChild().getLocation()));
                return node;
            }
            variable = ((KList)child).getContents().get(0);
        } else {
            variable = ((ListItem)item).getItem();
        }
        if (!(variable instanceof Variable))//&&    MetaK.isBuiltinSort(item.getItem().getSort())
                 {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING, 
                    KExceptionGroup.COMPILER, 
                    "Expecting an input type variable but got " + variable.getClass() + "." +
                            System.getProperty("line.separator") + "Won't transform.", 
                            getName(), variable.getFilename(), variable.getLocation()));
            return node;
        }            
        if ((!newList && !(rewrite.getRight() instanceof List && 
            ((List) rewrite.getRight()).isEmpty())) ||
            (newList && !(rewrite.getRight() instanceof KApp &&
            ((KApp)rewrite.getRight()).getLabel().equals(
                KLabelConstant.of(DataStructureSort.DEFAULT_LIST_UNIT_LABEL, context))))) {
            GlobalSettings.kem.register(new KException(ExceptionType.WARNING, 
                    KExceptionGroup.COMPILER, 
                    "Expecting an empty list but got " + rewrite.getRight().getClass() + " of sort " + 
                            rewrite.getRight().getSort() + "." +
                            System.getProperty("line.separator") + "Won't transform.", 
                            getName(), rewrite.getRight().getFilename(), rewrite.getRight().getLocation()));
            return node;                        
        }
        
        hasInputCell = true;
        resultCondition = getPredicateTerm((Variable)variable);;
        
        Term parseTerm = KApp.of(parseInputLabel, 
            StringBuiltin.kAppOf(variable.getSort()),
            StringBuiltin.kAppOf(inputCells.get(node.getLabel())));
        
        Term ioBuffer = KApp.of(bufferLabel,
           new Variable(Variable.getFreshVar("K")));
        
//        ctor(List)[replaceS[emptyCt(List),parseTerm(string(Ty),nilK)],ioBuffer(mkVariable('BI,K))]
        Term list;
        if (newList) {
            DataStructureSort myList = context.dataStructureListSortOf(
                DataStructureSort.DEFAULT_LIST_SORT);
            Term term1 = new Rewrite(
                KApp.of(KLabelConstant.of(myList.unitLabel(), context)),
                KApp.of(KLabelConstant.of(myList.elementLabel(), context), parseTerm),
                context);
            Term term2 = KApp.of(KLabelConstant.of(myList.elementLabel(), context), ioBuffer);
            list = KApp.of(KLabelConstant.of(myList.constructorLabel(), context), term1, term2);
        } else {
            list = new List();
            ((List)list).getContents().add(new Rewrite(List.EMPTY, new ListItem(parseTerm), context));
            ((List)list).getContents().add(new ListItem(ioBuffer));
        }
        
        node = node.shallowCopy();
        node.setContents(list);
        return node;
    }

    private static final KLabelConstant parseInputLabel = KLabelConstant.of("#parseInput");
    private static final KLabelConstant bufferLabel = KLabelConstant.of("#buffer");

    private Term getPredicateTerm(Variable var) {
        return KApp.of(KLabelConstant.KNEQ_KLABEL, KApp.of(KLabelConstant.STREAM_PREDICATE, var), BoolBuiltin.TRUE);
    }
}