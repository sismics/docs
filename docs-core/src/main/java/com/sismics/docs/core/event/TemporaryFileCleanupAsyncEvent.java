package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.model.jpa.File;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Cleanup temporary files event.
 *
 * @author bgamard
 */
public class TemporaryFileCleanupAsyncEvent {
    /**
     * Temporary files.
     */
    private List<Path> fileList;

    public TemporaryFileCleanupAsyncEvent(List<Path> fileList) {
        this.fileList = fileList;
    }

    public List<Path> getFileList() {
        return fileList;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("files", fileList)
            .toString();
    }
}