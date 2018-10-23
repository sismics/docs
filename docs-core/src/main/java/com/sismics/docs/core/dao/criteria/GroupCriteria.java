package com.sismics.docs.core.dao.criteria;

/**
 * Group criteria.
 *
 * @author bgamard 
 */
public class GroupCriteria {
    /**
     * Search query.
     */
    private String search;

    /**
     * User ID.
     */
    private String userId;
    
    /**
     * Retrieve user groups recursively.
     */
    private boolean recursive = false;
    
    public String getSearch() {
        return search;
    }

    public GroupCriteria setSearch(String search) {
        this.search = search;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public GroupCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public GroupCriteria setRecursive(boolean recursive) {
        this.recursive = recursive;
        return this;
    }
}
