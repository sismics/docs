package com.sismics.util.css;

import java.util.ArrayList;
import java.util.List;

/**
 * A CSS selector.
 * 
 * @author bgamard
 */
public class Selector {
    /**
     * Selector name.
     */
    private String name;
    
    /**
     * Rules in this selector.
     */
    private List<Rule> ruleList;
    
    /**
     * Create a new CSS selector.
     * 
     * @param name Selector name
     */
    public Selector(String name) {
        this.name = name;
        ruleList = new ArrayList<>();
    }
    
    /**
     * Add a CSS rule.
     * 
     * @param property Property name
     * @param value Value
     * @return CSS selector
     */
    public Selector rule(String property, String value) {
        ruleList.add(new Rule(property, value));
        return this;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append(" {\n");
        for (Rule rule : ruleList) {
            sb.append("  ");
            sb.append(rule);
            sb.append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
