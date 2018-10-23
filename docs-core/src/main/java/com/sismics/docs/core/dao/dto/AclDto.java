package com.sismics.docs.core.dao.dto;

import com.sismics.docs.core.constant.PermType;

/**
 * Acl DTO.
 *
 * @author bgamard 
 */
public class AclDto {
    /**
     * Acl ID.
     */
    private String id;
    
    /**
     * Target name.
     */
    private String targetName;
    
    /**
     * Permission.
     */
    private PermType perm;
    
    /**
     * Source ID.
     */
    private String sourceId;
    
    /**
     * Target ID.
     */
    private String targetId;
    
    /**
     * Target type.
     */
    private String targetType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public PermType getPerm() {
        return perm;
    }

    public void setPerm(PermType perm) {
        this.perm = perm;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
}
