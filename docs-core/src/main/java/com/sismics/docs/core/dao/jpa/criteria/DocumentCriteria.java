package com.sismics.docs.core.dao.jpa.criteria;

import java.util.Date;


/**
 * Document criteria.
 *
 * @author bgamard 
 */
public class DocumentCriteria {
    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Search query.
     */
    private String search;
    
    /**
     * Minimum creation date.
     */
    private Date createDateMin;
    
    /**
     * Maximum creation date.
     */
    private Date createDateMax;
    
    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of search.
     *
     * @return the search
     */
    public String getSearch() {
        return search;
    }

    /**
     * Setter of search.
     *
     * @param search search
     */
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Getter of createDateMin.
     *
     * @return the createDateMin
     */
    public Date getCreateDateMin() {
        return createDateMin;
    }

    /**
     * Setter of createDateMin.
     *
     * @param createDateMin createDateMin
     */
    public void setCreateDateMin(Date createDateMin) {
        this.createDateMin = createDateMin;
    }

    /**
     * Getter of createDateMax.
     *
     * @return the createDateMax
     */
    public Date getCreateDateMax() {
        return createDateMax;
    }

    /**
     * Setter of createDateMax.
     *
     * @param createDateMax createDateMax
     */
    public void setCreateDateMax(Date createDateMax) {
        this.createDateMax = createDateMax;
    }
}
