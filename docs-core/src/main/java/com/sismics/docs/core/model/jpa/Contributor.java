package com.sismics.docs.core.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Contributor entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_CONTRIBUTOR")
public class Contributor {
    /**
     * Contributor ID.
     */
    @Id
    @Column(name = "CTR_ID_C", length = 36)
    private String id;
    
    /**
     * Document ID.
     */
    @Column(name = "CTR_IDDOC_C", length = 36, nullable = false)
    private String documentId;
    
    /**
     * User ID.
     */
    @Column(name = "CTR_IDUSER_C", length = 36, nullable = false)
    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("documentId", documentId)
                .toString();
    }
}
