package com.sismics.docs.core.dao.criteria;


/**
 * Route criteria.
 *
 * @author bgamard
 */
public class RouteCriteria {
    /**
     * Document ID.
     */
    private String documentId;

    public String getDocumentId() {
        return documentId;
    }

    public RouteCriteria setDocumentId(String documentId) {
        this.documentId = documentId;
        return this;
    }
}
