package com.sismics.docs.event;

/**
 * @author bgamard.
 */
public class DocumentFullscreenEvent {

    private boolean fullscreen;

    public DocumentFullscreenEvent(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }
}
