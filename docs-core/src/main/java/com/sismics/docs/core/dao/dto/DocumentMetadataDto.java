package com.sismics.docs.core.dao.dto;

import com.sismics.docs.core.constant.MetadataType;

/**
 * Document metadata DTO.
 *
 * @author bgamard 
 */
public class DocumentMetadataDto {
    /**
     * Document metadata ID.
     */
    private String id;

    /**
     * Document ID.
     */
    private String documentId;

    /**
     * Metadata ID.
     */
    private String metadataId;
    
    /**
     * Name.
     */
    private String name;

    /**
     * Value.
     */
    private String value;

    /**
     * Type.
     */
    private MetadataType type;

    public String getId() {
        return id;
    }

    public DocumentMetadataDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DocumentMetadataDto setName(String name) {
        this.name = name;
        return this;
    }

    public MetadataType getType() {
        return type;
    }

    public DocumentMetadataDto setType(MetadataType type) {
        this.type = type;
        return this;
    }

    public String getDocumentId() {
        return documentId;
    }

    public DocumentMetadataDto setDocumentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getMetadataId() {
        return metadataId;
    }

    public DocumentMetadataDto setMetadataId(String metadataId) {
        this.metadataId = metadataId;
        return this;
    }

    public String getValue() {
        return value;
    }

    public DocumentMetadataDto setValue(String value) {
        this.value = value;
        return this;
    }
}
