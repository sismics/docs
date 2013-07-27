package com.sismics.docs.core.util.jpa;

/**
 * Sort criteria of a query.
 *
 * @author jtremeaux 
 */
public class SortCriteria {
    /**
     * Index of the column to sort (first is 0).
     */
    private int column;
    
    /**
     * Sort in increasing order (or else decreasing).
     */
    private boolean asc = true;

    /**
     * Constructor of sortCriteria.
     */
    public SortCriteria(Integer column, Boolean asc) {
        if (column != null) {
            this.column = column;
        }
        if (asc != null) {
            this.asc = asc;
        }
    }
    
    /**
     * Getter of column.
     *
     * @return column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Getter of asc.
     *
     * @return asc
     */
    public boolean isAsc() {
        return asc;
    }
}
