package com.sismics.docs.event;

import org.json.JSONObject;

/**
 * Share send event.
 *
 * @author bgamard.
 */
public class ShareSendEvent {
    /**
     * Share data.
     */
    private JSONObject share;

    /**
     * Create a share send event.
     *
     * @param share Share data
     */
    public ShareSendEvent(JSONObject share) {
        this.share = share;
    }

    public JSONObject getShare() {
        return share;
    }
}
