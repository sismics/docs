package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Group entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_GROUP")
public class Group implements Loggable {
    /**
     * Group ID.
     */
    @Id
    @Column(name = "GRP_ID_C", nullable = false, length = 36)
    private String id;
    
    /**
     * Vocabulary value.
     */
    @Column(name = "GRP_IDPARENT_C", length = 36)
    private String parentId;
    
    /**
     * Group name.
     */
    @Column(name = "GRP_NAME_C", nullable = false, length = 50)
    private String name;

    /**
     * Role ID.
     */
    @Column(name = "GRP_IDROLE_C", length = 36)
    private String roleId;
    
    /**
     * Deletion date.
     */
    @Column(name = "GRP_DELETEDATE_D")
    private Date deleteDate;
    
    public String getId() {
        return id;
    }

    public Group setId(String id) {
        this.id = id;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public Group setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }
    
    public Group setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }
    
    public String getRoleId() {
        return roleId;
    }

    public Group setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("roleId", roleId)
                .add("parentId", parentId)
                .add("name", name)
                .toString();
    }

    @Override
    public String toMessage() {
        return name;
    }
}
