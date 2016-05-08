package com.sismics.util.css;

/**
 * A CSS rule.
 * 
 * @author bgamard
 */
public class Rule {
    /**
     * Rule separator.
     */
    private static String SEPARATOR = ": ";
    
    /**
     * CSS rule property name.
     */
    private String property;
    
    /**
     * CSS rule value.
     */
    private String value;
    
    /**
     * Create a new CSS rule.
     * 
     * @param property Property name
     * @param value Value
     */
    public Rule(String property, String value) {
        this.property = property;
        this.value = value;
    }
    
    @Override
    public String toString() {
        return property + SEPARATOR + value;
    }
}
