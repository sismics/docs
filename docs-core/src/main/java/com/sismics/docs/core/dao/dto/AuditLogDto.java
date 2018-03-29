package com.sismics.docs.core.dao.dto;

import com.sismics.docs.core.constant.AuditLogType;

/**
 * Audit log DTO.
 *
 * @author bgamard 
 */
public class AuditLogDto {
    /**
     * Audit log ID.
     */
    private String id;
    
    /**
     * Username.
     */
    private String username;
    
    /**
     * Entity ID.
     */
    private String entityId;
    
    /**
     * Entity class.
     */
    private String entityClass;
    
    /**
     * Audit log type.
     */
    private AuditLogType type;
    
    /**
     * Audit log message.
     */
    private String message;
    
    /**
     * Creation date.
     */
    private Long createTimestamp;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }

    public AuditLogType getType() {
        return type;
    }

    public void setType(AuditLogType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }
}
