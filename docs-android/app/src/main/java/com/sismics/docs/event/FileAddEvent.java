package com.sismics.docs.event;

/**
 * File add event.
 *
 * @author bgamard.
 */
public class FileAddEvent {
    /**
     * Document ID.
     */
    private String documentId;
    /**
     * File ID.
     */
    private String fileId;

    /**
     * Create a file add event.
     *
     * @param fileId File ID
     */
    public FileAddEvent(String documentId, String fileId) {
        this.documentId = documentId;
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

    /**
     * Getter of documentId.
     *
     * @return documentId
     */
    public String getDocumentId() {
        return documentId;
    }
}
