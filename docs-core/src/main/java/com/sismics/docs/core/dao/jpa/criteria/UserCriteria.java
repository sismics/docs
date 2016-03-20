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
     * Group ID.
     */
    private String groupId;

    public String getSearch() {
        return search;
    }

    public UserCriteria setSearch(String search) {
        this.search = search;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public UserCriteria setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }
}
