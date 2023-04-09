package com.sismics.docs.core.model.jpa;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Role (set of base functions).
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_ROLE")
public class Role {
    /**
     * Role ID.
     */
    @Id
    @Column(name = "ROL_ID_C", length = 36)
    private String id;
    
    /**
     * Role name.
     */
    @Column(name = "ROL_NAME_C", nullable = false, length = 50)
    private String name;
    
    /**
     * Creation date.
     */
    @Column(name = "ROL_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "ROL_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
                .add("name", name)
                .toString();
    }
}
