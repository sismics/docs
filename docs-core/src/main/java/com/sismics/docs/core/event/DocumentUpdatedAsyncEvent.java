package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.model.jpa.Document;

/**
 * Document updated event.
 *
 * @author bgamard
 */
public class DocumentUpdatedAsyncEvent {
    /**
     * Created document.
     */
    private Document document;
    
    /**
     * Getter of document.
     *
     * @return the document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Setter of document.
     *
     * @param document document
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("document", document)
            .toString();
    }
}