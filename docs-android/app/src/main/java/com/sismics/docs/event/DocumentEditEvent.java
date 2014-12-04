package com.sismics.docs.event;

import org.json.JSONObject;

/**
 * Document edit event.
 *
 * @author bgamard.
 */
public class DocumentEditEvent {
    /**
     * Document.
     */
    private JSONObject document;

    /**
     * Create a document edit event.
     *
     * @param document Document
     */
    public DocumentEditEvent(JSONObject document) {
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
