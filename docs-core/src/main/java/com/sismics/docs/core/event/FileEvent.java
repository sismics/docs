package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.model.jpa.File;

import java.nio.file.Path;

/**
 * New file event.
 *
 * @author bgamard
 */
public abstract class FileEvent extends UserEvent {
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

    public FileEvent setUnencryptedFile(Path unencryptedFile) {
        this.unencryptedFile = unencryptedFile;
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