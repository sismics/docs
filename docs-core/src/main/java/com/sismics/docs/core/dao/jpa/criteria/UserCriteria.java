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
    public UserCriteria setSearch(String search) {
        this.search = search;
        return this;
    }
}
