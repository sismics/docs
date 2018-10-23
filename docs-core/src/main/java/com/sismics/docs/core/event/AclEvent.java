package com.sismics.docs.core.event;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.constant.PermType;

/**
 * ACL event.
 *
 * @author bgamard
 */
public abstract class AclEvent extends UserEvent {
    /**
     * Source ID.
     */
    private String sourceId;

    /**
     * Permission type.
     */
    private PermType perm;

    /**
     * Target ID.
     */
    private String targetId;

    public String getSourceId() {
        return sourceId;
    }

    public AclEvent setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public PermType getPerm() {
        return perm;
    }

    public AclEvent setPerm(PermType permType) {
        this.perm = permType;
        return this;
    }

    public String getTargetId() {
        return targetId;
    }

    public AclEvent setTargetId(String targetId) {
        this.targetId = targetId;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sourceId", sourceId)
                .add("perm", perm)
                .add("targetId", targetId)
                .toString();
    }
}
