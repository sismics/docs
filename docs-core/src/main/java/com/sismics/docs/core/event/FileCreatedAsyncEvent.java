package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.model.jpa.File;

import java.nio.file.Path;

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
     * Unencrypted original file.
     */
    private Path unencryptedFile;
    
    /**
     * Unencrypted file containing PDF representation
     * of the original file. May be null if the PDF conversion is not
     * necessary or not possible.
     */
    private Path unencryptedPdfFile;
    
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

    public Path getUnencryptedFile() {
        return unencryptedFile;
    }

    public FileCreatedAsyncEvent setUnencryptedFile(Path unencryptedFile) {
        this.unencryptedFile = unencryptedFile;
        return this;
    }

    public Path getUnencryptedPdfFile() {
        return unencryptedPdfFile;
    }

    public FileCreatedAsyncEvent setUnencryptedPdfFile(Path unencryptedPdfFile) {
        this.unencryptedPdfFile = unencryptedPdfFile;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("file", file)
            .add("language", language)
            .toString();
    }
}