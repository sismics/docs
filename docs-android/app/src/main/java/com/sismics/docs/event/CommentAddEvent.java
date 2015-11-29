package com.sismics.docs.event;

import org.json.JSONObject;

/**
 * Comment add event.
 *
 * @author bgamard.
 */
public class CommentAddEvent {
    /**
     * Comment.
     */
    private JSONObject comment;

    /**
     * Create a comment add event.
     *
     * @param comment Comment
     */
    public CommentAddEvent(JSONObject comment) {
        this.comment = comment;
    }

    /**
     * Getter of comment.
     *
     * @return comment
     */
    public JSONObject getComment() {
        return comment;
    }
}
