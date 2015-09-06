package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.model.jpa.File;

/**
 * File deleted event.
 *
 * @author bgamard
 */
public class FileDeletedAsyncEvent {
    /**
     * Deleted file.
     */
    private File file;
    
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
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("file", file)
            .toString();
    }
}