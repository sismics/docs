package com.sismics.docs.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Tag.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_TAG")
public class Tag {
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
    @Column(name = "TAG_COLOR_C", nullable = false, length = 6)
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

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }
}
