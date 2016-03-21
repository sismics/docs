package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.model.jpa.File;

/**
 * File deleted event.
 *
 * @author bgamard
 */
public class FileDeletedAsyncEvent extends UserEvent {
    /**
     * Deleted file.
     */
    private File file;
    
    public File getFile() {
        return file;
    }

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