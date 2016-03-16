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
     * Deletion date.
     */
    @Column(name = "GRP_DELETEDATE_D")
    private Date deleteDate;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
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
                .add("parentId", parentId)
                .add("name", name)
                .toString();
    }

    @Override
    public String toMessage() {
        return name;
    }
}
