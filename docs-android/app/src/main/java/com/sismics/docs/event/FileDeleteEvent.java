package com.sismics.docs.event;

/**
 * File delete event.
 *
 * @author bgamard.
 */
public class FileDeleteEvent {
    /**
     * File ID.
     */
    private String fileId;

    /**
     * Create a document delete event.
     *
     * @param fileId File ID
     */
    public FileDeleteEvent(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Getter of fileId.
     *
     * @return fileId
     */
    public String getFileId() {
        return fileId;
    }
}
