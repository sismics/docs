package com.sismics.docs.event;

/**
 * Share delete event.
 *
 * @author bgamard.
 */
public class ShareDeleteEvent {
    /**
     * Share ID
     */
    private String id;

    /**
     * Create a share delete event.
     *
     * @param id Share ID
     */
    public ShareDeleteEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
