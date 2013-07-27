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
}
