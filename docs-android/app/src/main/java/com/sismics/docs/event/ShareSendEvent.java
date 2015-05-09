package com.sismics.docs.event;

import org.json.JSONObject;

/**
 * Share send event.
 *
 * @author bgamard.
 */
public class ShareSendEvent {
    /**
     * ACL data.
     */
    private JSONObject acl;

    /**
     * Create a share send event.
     *
     * @param acl ACL data
     */
    public ShareSendEvent(JSONObject acl) {
        this.acl = acl;
    }

    public JSONObject getAcl() {
        return acl;
    }
}
