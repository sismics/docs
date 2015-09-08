package com.sismics.docs.core.event;

import java.io.InputStream;

import com.google.common.base.MoreObjects;
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
     * Unencrypted input stream containing the file.
     */
    private InputStream inputStream;
    
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
    
    /**
     * Getter of inputStream.
     *
     * @return the inputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Setter de inputStream.
     *
     * @param inputStream inputStream
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("file", file)
            .add("document", document)
            .toString();
    }
}