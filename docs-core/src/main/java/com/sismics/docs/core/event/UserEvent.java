package com.sismics.docs.core.event;

/**
 * Event fired by a user.
 * 
 * @author bgamard
 */
public abstract class UserEvent {
    /**
     * User ID who fired the event.
     */
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
