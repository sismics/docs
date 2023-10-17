package com.sismics.docs.core.model.jpa;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Link between a document and a tag.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_DOCUMENT_TAG")
public class DocumentTag implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Document tag ID.
     */
    @Id
    @Column(name = "DOT_ID_C", length = 36)
    private String id;
    
    /**
     * Document ID.
     */
    @Column(name = "DOT_IDDOCUMENT_C", nullable = false, length = 36)
    private String documentId;
    
    /**
     * Tag ID.
     */
    @Column(name = "DOT_IDTAG_C", nullable = false, length = 36)
    private String tagId;

    /**
     * Deletion date.
     */
    @Column(name = "DOT_DELETEDATE_D")
    private Date deleteDate;
    
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

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }
    
    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("documentId", documentId)
                .add("tagId", tagId)
                .toString();
    }
}
