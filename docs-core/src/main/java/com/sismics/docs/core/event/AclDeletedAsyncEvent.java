package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;

/**
 * ACL deleted event.
 *
 * @author bgamard
 */
public class AclDeletedAsyncEvent extends UserEvent {
    /**
     * Source ID.
     */
    private String sourceId;

    public String getSourceId() {
        return sourceId;
    }

    public AclDeletedAsyncEvent setSourceId(String sourceId) {
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