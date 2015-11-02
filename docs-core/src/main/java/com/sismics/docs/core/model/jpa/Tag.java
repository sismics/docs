package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.util.AuditLogUtil;

/**
 * Tag.
 * 
 * @author bgamard
 */
@Entity
@EntityListeners(AuditLogUtil.class)
@Table(name = "T_TAG")
public class Tag implements Loggable {
    /**
     * Tag ID.
     */
    @Id
    @Column(name = "TAG_ID_C", length = 36)
    private String id;
    
    /**
     * Tag name.
     */
    @Column(name = "TAG_NAME_C", nullable = false, length = 36)
    private String name;
    
    /**
     * User ID.
     */
    @Column(name = "TAG_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * User ID.
     */
    @Column(name = "TAG_IDPARENT_C", length = 36)
    private String parentId;
    
    /**
     * Creation date.
     */
    @Column(name = "TAG_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "TAG_DELETEDATE_D")
    private Date deleteDate;
    
    /**
     * Tag name.
     */
    @Column(name = "TAG_COLOR_C", nullable = false, length = 7)
    private String color;

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
     * Getter of userId.
     *
     * @return the userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter of name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
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
    
    /**
     * Getter of color.
     *
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * Setter of color.
     *
     * @param color color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Getter of deleteDate.
     *
     * @return deleteDate
     */
    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    /**
     * Setter of deleteDate.
     *
     * @param deleteDate deleteDate
     */
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    /**
     * Getter of parentId.
     *
     * @return parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Setter of parentId.
     *
     * @param parentId parentId
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("parentId", parentId)
                .toString();
    }

    @Override
    public String toMessage() {
        return name;
    }
}
