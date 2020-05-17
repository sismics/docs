package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;

/**
 * File deleted event.
 *
 * @author bgamard
 */
public class FileDeletedAsyncEvent extends UserEvent {
    /**
     * File ID.
     */
    private String fileId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("fileId", fileId)
            .toString();
    }
}