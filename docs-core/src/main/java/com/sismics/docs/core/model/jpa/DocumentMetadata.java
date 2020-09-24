package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Link between a document and a metadata, holding the value.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_DOCUMENT_METADATA")
public class DocumentMetadata implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Document metadata ID.
     */
    @Id
    @Column(name = "DME_ID_C", length = 36)
    private String id;
    
    /**
     * Document ID.
     */
    @Column(name = "DME_IDDOCUMENT_C", nullable = false, length = 36)
    private String documentId;
    
    /**
     * Metadata ID.
     */
    @Column(name = "DME_IDMETADATA_C", nullable = false, length = 36)
    private String metadataId;

    /**
     * Value.
     */
    @Column(name = "DME_VALUE_C", length = 4000)
    private String value;

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

    public String getMetadataId() {
        return metadataId;
    }

    public DocumentMetadata setMetadataId(String metadataId) {
        this.metadataId = metadataId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public DocumentMetadata setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("documentId", documentId)
                .add("metadataId", metadataId)
                .toString();
    }
}
