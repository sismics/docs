package com.sismics.docs.event;

/**
 * Document delete event.
 *
 * @author bgamard.
 */
public class DocumentDeleteEvent {
    /**
     * Document ID.
     */
    private String documentId;

    /**
     * Create a document delete event.
     *
     * @param documentId Document ID
     */
    public DocumentDeleteEvent(String documentId) {
        this.documentId = documentId;
    }

    /**
     * Getter of documentId.
     *
     * @return documentId
     */
    public String getDocumentId() {
        return documentId;
    }
}
