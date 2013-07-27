package com.sismics.docs.core.util.jpa;

import java.util.List;

/**
 * Paginated list.
 *
 * @author jtremeaux
 */
public class PaginatedList<T> {
    /**
     * Size of a page.
     */
    private int limit;

    /**
     * Offset of the page (in number of records).
     */
    private int offset;

    /**
     * Total number of records.
     */
    private int resultCount;
    
    /**
     * List of records of the current page.
     */
    private List<T> resultList;
    
    /**
     * Constructor of PaginatedList.
     * 
     * @param pageSize Page size
     * @param offset Offset
     */
    public PaginatedList(int pageSize, int offset) {
        this.limit = pageSize;
        this.offset = offset;
    }

    /**
     * Getter of resultCount.
     *
     * @return resultCount
     */
    public int getResultCount() {
        return resultCount;
    }

    /**
     * Setter of resultCount.
     *
     * @param resultCount resultCount
     */
    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }

    /**
     * Getter of resultList.
     *
     * @return resultList
     */
    public List<T> getResultList() {
        return resultList;
    }

    /**
     * Setter of resultList.
     *
     * @param resultList resultList
     */
    public void setResultList(List<T> resultList) {
        this.resultList = resultList;
    }

    /**
     * Getter of limit.
     *
     * @return limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Getter of offset.
     *
     * @return offset
     */
    public int getOffset() {
        return offset;
    }
}
