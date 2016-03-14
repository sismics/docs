package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;

/**
 * Document deleted event.
 *
 * @author bgamard
 */
public class DocumentDeletedAsyncEvent extends UserEvent {
    /**
     * Document ID.
     */
    private String documentId;
    
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("documentId", documentId)
            .toString();
    }
}