package com.sismics.docs.core.dao.jpa.criteria;


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
}
