package com.sismics.docs.core.dao.jpa.criteria;

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
     * User ID.
     */
    private String userId;

    /**
     * Document ID.
     */
    private String documentId;

    /**
     * Tag name.
     */
    private String name;

    /**
     * Approximate tag name.
     */
    private String nameLike;

    public String getId() {
        return id;
    }

    public TagCriteria setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public TagCriteria setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getDocumentId() {
        return documentId;
    }

    public TagCriteria setDocumentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getName() {
        return name;
    }

    public TagCriteria setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameLike() {
        return nameLike;
    }

    public TagCriteria setNameLike(String nameLike) {
        this.nameLike = nameLike;
        return this;
    }
}
