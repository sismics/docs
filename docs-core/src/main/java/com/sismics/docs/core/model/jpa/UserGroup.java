package com.sismics.docs.core.model.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Link between an user and a group.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_USER_GROUP")
public class UserGroup implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * User group ID.
     */
    @Id
    @Column(name = "UGP_ID_C", length = 36)
    private String id;
    
    /**
     * User ID.
     */
    @Column(name = "UGP_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Group ID.
     */
    @Column(name = "UGP_IDGROUP_C", nullable = false, length = 36)
    private String groupId;

    /**
     * Deletion date.
     */
    @Column(name = "UGP_DELETEDATE_D")
    private Date deleteDate;
    
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("groupId", groupId)
                .toString();
    }
}
