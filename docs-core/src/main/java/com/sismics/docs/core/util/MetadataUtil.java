package com.sismics.docs.core.util;

import com.google.common.collect.Maps;
import com.sismics.docs.core.constant.MetadataType;
import com.sismics.docs.core.dao.DocumentMetadataDao;
import com.sismics.docs.core.dao.MetadataDao;
import com.sismics.docs.core.dao.criteria.MetadataCriteria;
import com.sismics.docs.core.dao.dto.DocumentMetadataDto;
import com.sismics.docs.core.dao.dto.MetadataDto;
import com.sismics.docs.core.model.jpa.DocumentMetadata;
import com.sismics.docs.core.util.jpa.SortCriteria;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Metadata utilities.
 *
 * @author bgamard
 */
public class MetadataUtil {
    /**
     * Update custom metadata on a document.
     *
     * @param documentId Document ID
     * @param metadataIdList Metadata ID list
     * @param metadataValueList Metadata value list
     */
    public static void updateMetadata(String documentId, List<String> metadataIdList, List<String> metadataValueList) throws Exception {
        if (metadataIdList == null || metadataValueList == null || metadataIdList.isEmpty()) {
            return;
        }
        if (metadataIdList.size() != metadataValueList.size()) {
            throw new Exception("metadata_id and metadata_value must have the same length");
        }

        Map<String, String> newValues = Maps.newHashMap();
        for (int i = 0; i < metadataIdList.size(); i++) {
            newValues.put(metadataIdList.get(i), metadataValueList.get(i));
        }

        MetadataDao metadataDao = new MetadataDao();
        DocumentMetadataDao documentMetadataDao = new DocumentMetadataDao();
        List<MetadataDto> metadataDtoList = metadataDao.findByCriteria(new MetadataCriteria(), null);
        List<DocumentMetadataDto> documentMetadataDtoList = documentMetadataDao.getByDocumentId(documentId);

        // Update existing values
        for (DocumentMetadataDto documentMetadataDto : documentMetadataDtoList) {
            if (newValues.containsKey(documentMetadataDto.getMetadataId())) {
                // Update the value
                String value = newValues.get(documentMetadataDto.getMetadataId());
                validateValue(documentMetadataDto.getType(), value);
                updateValue(documentMetadataDto.getId(), value);
                newValues.remove(documentMetadataDto.getMetadataId());
            } else {
                // Remove the value
                updateValue(documentMetadataDto.getId(), null);
            }
        }

        // Create new values
        for (Map.Entry<String, String> entry : newValues.entrySet()) {
            // Search the metadata definition
            MetadataDto metadata = null;
            for (MetadataDto metadataDto : metadataDtoList) {
                if (metadataDto.getId().equals(entry.getKey())) {
                    metadata = metadataDto;
                    break;
                }
            }

            if (metadata == null) {
                throw new Exception(MessageFormat.format("Metadata not found: {0}", entry.getKey()));
            }

            // Add the value
            validateValue(metadata.getType(), entry.getValue());
            createValue(documentId, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Validate a custom metadata value.
     *
     * @param type Metadata type
     * @param value Value
     * @throws Exception In case of validation error
     */
    private static void validateValue(MetadataType type, String value) throws Exception {
        switch (type) {
            case STRING:
            case BOOLEAN:
                return;
            case DATE:
                try {
                    Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new Exception("Date value not parsable as timestamp");
                }
                break;
            case FLOAT:
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new Exception("Float value not parsable");
                }
                break;
            case INTEGER:
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new Exception("Integer value not parsable");
                }
                break;
        }
    }

    /**
     * Create a custom metadata value on a document.
     *
     * @param documentId Document ID
     * @param metadataId Metadata ID
     * @param value Value
     */
    private static void createValue(String documentId, String metadataId, String value) {
        DocumentMetadataDao documentMetadataDao = new DocumentMetadataDao();
        DocumentMetadata documentMetadata = new DocumentMetadata();
        documentMetadata.setDocumentId(documentId);
        documentMetadata.setMetadataId(metadataId);
        documentMetadata.setValue(value);
        documentMetadataDao.create(documentMetadata);
    }

    /**
     * Update a custom metadata value.
     *
     * @param documentMetadataId Document metadata ID
     * @param value Value
     */
    private static void updateValue(String documentMetadataId, String value) {
        DocumentMetadataDao documentMetadataDao = new DocumentMetadataDao();
        DocumentMetadata documentMetadata = new DocumentMetadata();
        documentMetadata.setId(documentMetadataId);
        documentMetadata.setValue(value);
        documentMetadataDao.update(documentMetadata);
    }

    /**
     * Add custom metadata to a JSON response.
     *
     * @param json JSON
     * @param documentId Document ID
     */
    public static void addMetadata(JsonObjectBuilder json, String documentId) {
        DocumentMetadataDao documentMetadataDao = new DocumentMetadataDao();
        MetadataDao metadataDao = new MetadataDao();
        List<MetadataDto> metadataDtoList = metadataDao.findByCriteria(new MetadataCriteria(), new SortCriteria(1, true));
        List<DocumentMetadataDto> documentMetadataDtoList = documentMetadataDao.getByDocumentId(documentId);
        JsonArrayBuilder metadata = Json.createArrayBuilder();
        for (MetadataDto metadataDto : metadataDtoList) {
            JsonObjectBuilder meta = Json.createObjectBuilder()
                    .add("id", metadataDto.getId())
                    .add("name", metadataDto.getName())
                    .add("type", metadataDto.getType().name());
            for (DocumentMetadataDto documentMetadataDto : documentMetadataDtoList) {
                if (documentMetadataDto.getMetadataId().equals(metadataDto.getId())) {
                    if (documentMetadataDto.getValue() != null) {
                        switch (metadataDto.getType()) {
                            case STRING:
                                meta.add("value", documentMetadataDto.getValue());
                                break;
                            case BOOLEAN:
                                meta.add("value", Boolean.parseBoolean(documentMetadataDto.getValue()));
                                break;
                            case DATE:
                                meta.add("value", Long.parseLong(documentMetadataDto.getValue()));
                                break;
                            case FLOAT:
                                meta.add("value", Double.parseDouble(documentMetadataDto.getValue()));
                                break;
                            case INTEGER:
                                meta.add("value", Integer.parseInt(documentMetadataDto.getValue()));
                                break;
                        }
                    }
                }
            }
            metadata.add(meta);
        }
        json.add("metadata", metadata);
    }
}
