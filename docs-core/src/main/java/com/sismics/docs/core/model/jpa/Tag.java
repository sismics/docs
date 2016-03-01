package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Tag.
 * 
 * @author bgamard
 */
@Entity
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    public String getParentId() {
        return parentId;
    }

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
