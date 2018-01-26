package com.sismics.docs.core.dao.jpa.criteria;


/**
 * Audit log criteria.
 *
 * @author bgamard 
 */
public class AuditLogCriteria {
    /**
     * Document ID.
     */
    private String documentId;

    /**
     * User ID.
     */
    private String userId;
    
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
