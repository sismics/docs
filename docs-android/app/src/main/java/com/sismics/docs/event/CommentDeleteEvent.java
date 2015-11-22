package com.sismics.docs.event;

/**
 * Comment delete event.
 *
 * @author bgamard.
 */
public class CommentDeleteEvent {
    /**
     * Comment ID.
     */
    private String commentId;

    /**
     * Create a comment add event.
     *
     * @param commentId Comment ID
     */
    public CommentDeleteEvent(String commentId) {
        this.commentId = commentId;
    }

    /**
     * Getter of commentId.
     *
     * @return commentId
     */
    public String getCommentId() {
        return commentId;
    }
}
