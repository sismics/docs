package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;

import java.nio.file.Path;

/**
 * New file event.
 *
 * @author bgamard
 */
public abstract class FileEvent extends UserEvent {
    /**
     * File ID.
     */
    private String fileId;
    
    /**
     * Language of the file.
     */
    private String language;
    
    /**
     * Unencrypted original file.
     */
    private Path unencryptedFile;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
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

    public FileEvent setUnencryptedFile(Path unencryptedFile) {
        this.unencryptedFile = unencryptedFile;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("fileId", fileId)
            .add("language", language)
            .toString();
    }
}