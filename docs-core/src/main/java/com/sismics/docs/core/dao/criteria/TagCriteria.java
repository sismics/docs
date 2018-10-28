package com.sismics.docs.core.dao.criteria;

import java.util.List;

/**
 * Tag criteria.
 *
 * @author bgamard 
 */
public class TagCriteria {
    /**
     * Tag ID.
     */
    private String id;

    /**
     * ACL target ID list.
     */
    private List<String> targetIdList;

    /**
     * Document ID.
     */
    private String documentId;

    public String getId() {
        return id;
    }

    public TagCriteria setId(String id) {
        this.id = id;
        return this;
    }

    public List<String> getTargetIdList() {
        return targetIdList;
    }

    public TagCriteria setTargetIdList(List<String> targetIdList) {
        this.targetIdList = targetIdList;
        return this;
    }

    public String getDocumentId() {
        return documentId;
    }

    public TagCriteria setDocumentId(String documentId) {
        this.documentId = documentId;
        return this;
    }
}
