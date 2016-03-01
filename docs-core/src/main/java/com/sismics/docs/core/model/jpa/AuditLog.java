package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.constant.AuditLogType;

/**
 * Audit log.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_AUDIT_LOG")
public class AuditLog {
    /**
     * Audit log ID.
     */
    @Id
    @Column(name = "LOG_ID_C", length = 36)
    private String id;
    
    /**
     * User ID.
     */
    @Column(name = "LOG_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Entity ID.
     */
    @Column(name = "LOG_IDENTITY_C", nullable = false, length = 36)
    private String entityId;
    
    /**
     * Entity class.
     */
    @Column(name = "LOG_CLASSENTITY_C", nullable = false, length = 50)
    private String entityClass;
    
    /**
     * Audit log type.
     */
    @Column(name = "LOG_TYPE_C", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditLogType type;
    
    /**
     * Audit log message.
     */
    @Column(name = "LOG_MESSAGE_C", length = 1000)
    private String message;
    
    /**
     * Creation date.
     */
    @Column(name = "LOG_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public AuditLogType getType() {
        return type;
    }

    public void setType(AuditLogType type) {
        this.type = type;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("entityId", entityId)
                .add("entityClass", entityClass)
                .add("type", type)
                .toString();
    }
}
