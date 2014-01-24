package org.kframework.backend.java.kil;

import org.kframework.backend.java.symbolic.Transformer;
import org.kframework.backend.java.symbolic.Visitor;
import org.kframework.kil.ASTNode;
import org.kframework.kil.Attribute;
import org.kframework.kil.loader.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;


/**
 * A K definition in the format of the Java Rewrite Engine.
 *
 * @author AndreiS
 */
public class Definition extends JavaSymbolicObject {

    public static final Set<String> TOKEN_SORTS = ImmutableSet.of(
            "Bool",
            "Int",
            "String",
            "Id");

    private final List<Rule> rules;
    private final List<Rule> macros;
    private final Multimap<KLabelConstant, Rule> functionRules = ArrayListMultimap.create();
    private final Set<KLabelConstant> kLabels;
    private final Set<KLabelConstant> frozenKLabels;
    private final Context context;

    public Definition(Context context) {
        this.context = context;
        rules = new ArrayList<Rule>();
        macros = new ArrayList<Rule>();
        kLabels = new HashSet<KLabelConstant>();
        frozenKLabels = new HashSet<KLabelConstant>();
    }

    public void addFrozenKLabel(KLabelConstant frozenKLabel) {
        frozenKLabels.add(frozenKLabel);
    }

    public void addFrozenKLabelCollection(Collection<KLabelConstant> frozenKLabels) {
        for (KLabelConstant frozenKLabel : frozenKLabels) {
            this.frozenKLabels.add(frozenKLabel);
        }
    }

    public void addKLabel(KLabelConstant kLabel) {
        kLabels.add(kLabel);
    }

    public void addKLabelCollection(Collection<KLabelConstant> kLabels) {
        for (KLabelConstant kLabel : kLabels) {
            this.kLabels.add(kLabel);
        }
    }

    public void addRule(Rule rule) {
        if (rule.containsAttribute(Attribute.FUNCTION_KEY)) {
            functionRules.put(rule.functionKLabel(), rule);
        } else if (rule.containsAttribute(Attribute.MACRO_KEY)) {
            macros.add(rule);
        } else {
            rules.add(rule);
        }
    }

    public void addRuleCollection(Collection<Rule> rules) {
        for (Rule rule : rules) {
            addRule(rule);
        }
    }

    public Context context() {
        return context;
    }

    public Multimap<KLabelConstant, Rule> functionRules() {
        return functionRules;
    }

    public Set<KLabelConstant> frozenKLabels() {
        return frozenKLabels;
    }

    public Set<KLabelConstant> kLabels() {
        return Collections.unmodifiableSet(kLabels);
    }

    public List<Rule> macros() {
        return Collections.unmodifiableList(macros);
    }

    public Collection<Rule> rules() {
        return Collections.unmodifiableList(rules);
    }

    @Override
    public ASTNode accept(Transformer transformer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void accept(Visitor visitor) {
        throw new UnsupportedOperationException();
    }

}
