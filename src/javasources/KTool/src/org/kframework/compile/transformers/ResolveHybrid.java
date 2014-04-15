package org.kframework.compile.transformers;

import org.kframework.kil.*;
import org.kframework.kil.loader.Context;
import org.kframework.kil.visitors.CopyOnWriteTransformer;
import org.kframework.kil.visitors.exceptions.TransformerException;

import java.util.ArrayList;
import java.util.List;

public class ResolveHybrid extends CopyOnWriteTransformer {

    private List<ModuleItem> hybrids = new ArrayList<ModuleItem>();

    public ResolveHybrid(Context context) {
        super("Resolve Hybrid", context);
    }
    
    
    @Override
    public ASTNode visit(Module node, Void _) throws TransformerException {
        hybrids.clear();
        super.visit(node, _);
        if (hybrids.isEmpty()) return node;
        node = node.shallowCopy();
        hybrids.addAll(node.getItems());
        node.setItems(hybrids);
        return node;
    }
    
    @Override
    public ASTNode visit(Production node, Void _) throws TransformerException {
        if (!node.containsAttribute("hybrid")) return node;
        Rule rule = new Rule();
        rule.setBody(new Rewrite(
                KApp.of(KLabelConstant.KRESULT_PREDICATE,
                        new KApp(KLabelConstant.of(((Terminal) node.getItems().get(0)).getTerminal(),
                                context),
                                 new Variable("Ks", KSorts.KLIST))),
                BoolBuiltin.TRUE, context));
        rule.setRequires(new KApp(
                KLabelConstant.KRESULT_PREDICATE,
                new Variable("Ks", KSorts.KLIST)));

        rule.addAttribute(Attribute.PREDICATE);
        hybrids.add(rule);
        return node;
    }
    
    @Override
    public ASTNode visit(Configuration node, Void _) throws TransformerException {
        
        return node;
    }
    
    @Override
    public ASTNode visit(org.kframework.kil.Context node, Void _) throws TransformerException {
        
        return node;
    }
    
    @Override
    public ASTNode visit(Rule node, Void _) throws TransformerException {
        
        return node;
    }
    
 
}
