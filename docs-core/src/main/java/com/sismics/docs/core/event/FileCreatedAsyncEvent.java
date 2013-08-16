package com.sismics.docs.core.event;

import com.google.common.base.Objects;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;

/**
 * New file created event.
 *
 * @author bgamard
 */
public class FileCreatedAsyncEvent {
    /**
     * Created file.
     */
    private File file;
    
    /**
     * Document linked to the file.
     */
    private Document document;
    
    /**
     * Getter of file.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Setter of file.
     *
     * @param file file
     */
    public void setFile(File file) {
        this.file = file;
    }
    
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
        return Objects.toStringHelper(this)
            .add("file", file)
            .add("document", document)
            .toString();
    }
}