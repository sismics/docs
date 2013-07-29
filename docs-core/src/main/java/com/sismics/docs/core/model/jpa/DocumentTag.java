package com.sismics.docs.core.model.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

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
    @Id
    @Column(name = "DOT_IDDOCUMENT_C", length = 36)
    private String documentId;
    
    /**
     * Tag ID.
     */
    @Id
    @Column(name = "DOT_IDTAG_C", length = 36)
    private String tagId;

    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Getter de documentId.
     *
     * @return the documentId
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Setter de documentId.
     *
     * @param documentId documentId
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Getter de tagId.
     *
     * @return the tagId
     */
    public String getTagId() {
        return tagId;
    }

    /**
     * Setter de tagId.
     *
     * @param tagId tagId
     */
    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("documentId", documentId)
                .add("tagId", tagId)
                .toString();
    }
}
