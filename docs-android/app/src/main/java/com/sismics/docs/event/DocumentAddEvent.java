package com.sismics.docs.event;

import org.json.JSONObject;

/**
 * Document add event.
 *
 * @author bgamard.
 */
public class DocumentAddEvent {
    /**
     * Document.
     */
    private JSONObject document;

    /**
     * Create a document add event.
     *
     * @param document Document
     */
    public DocumentAddEvent(JSONObject document) {
        this.document = document;
    }

    /**
     * Getter of document.
     *
     * @return document
     */
    public JSONObject getDocument() {
        return document;
    }
}
