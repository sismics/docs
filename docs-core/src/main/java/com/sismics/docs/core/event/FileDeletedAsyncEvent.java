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

    private Long fileSize;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("fileId", fileId)
            .add("fileSize", fileSize)
            .toString();
    }
}
