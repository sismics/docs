package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;

/**
 * ACL created event.
 *
 * @author bgamard
 */
public class AclCreatedAsyncEvent extends UserEvent {
    /**
     * Source ID.
     */
    private String sourceId;

    public String getSourceId() {
        return sourceId;
    }

    public AclCreatedAsyncEvent setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("sourceId", sourceId)
            .toString();
    }
}