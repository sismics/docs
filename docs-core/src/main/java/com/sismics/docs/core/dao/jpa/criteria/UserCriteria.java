package com.sismics.docs.core.dao.jpa.criteria;

/**
 * User criteria.
 *
 * @author bgamard 
 */
public class UserCriteria {
    /**
     * Search query.
     */
    private String search;

    public String getSearch() {
        return search;
    }

    public UserCriteria setSearch(String search) {
        this.search = search;
        return this;
    }
}
