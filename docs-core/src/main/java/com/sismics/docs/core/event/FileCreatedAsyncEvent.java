package com.sismics.docs.core.event;

import java.io.InputStream;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.model.jpa.File;

/**
 * New file created event.
 *
 * @author bgamard
 */
public class FileCreatedAsyncEvent extends UserEvent {
    /**
     * Created file.
     */
    private File file;
    
    /**
     * Language of the file.
     */
    private String language;
    
    /**
     * Unencrypted input stream containing the file.
     */
    private InputStream inputStream;
    
    /**
     * Unencrypted input stream containing a PDF representation
     * of the file. May be null if the PDF conversion is not
     * necessary or not possible.
     */
    private InputStream pdfInputStream;
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    public InputStream getPdfInputStream() {
        return pdfInputStream;
    }

    public void setPdfInputStream(InputStream pdfInputStream) {
        this.pdfInputStream = pdfInputStream;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("file", file)
            .add("language", language)
            .toString();
    }
}