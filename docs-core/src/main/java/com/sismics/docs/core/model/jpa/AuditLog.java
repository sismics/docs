package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;
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
    
    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of entityId.
     * 
     * @return entityId
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Setter of entityId.
     *
     * @param entityId entityId
     */
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    /**
     * Getter of entityClass.
     * 
     * @return entityClass
     */
    public String getEntityClass() {
        return entityClass;
    }

    /**
     * Setter of entityClass.
     *
     * @param entityClass entityClass
     */
    public void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }
    
    /**
     * Getter of message.
     * 
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter of message.
     *
     * @param message message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Getter of type.
     * 
     * @return type
     */
    public AuditLogType getType() {
        return type;
    }

    /**
     * Setter of type.
     *
     * @param type type
     */
    public void setType(AuditLogType type) {
        this.type = type;
    }

    /**
     * Getter of createDate.
     *
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     *
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("entityId", entityId)
                .add("entityClass", entityClass)
                .add("type", type)
                .toString();
    }
}
