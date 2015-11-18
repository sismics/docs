package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Comment entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_COMMENT")
public class Comment implements Loggable {
    /**
     * Comment ID.
     */
    @Id
    @Column(name = "COM_ID_C", length = 36)
    private String id;
    
    /**
     * Document ID.
     */
    @Column(name = "COM_IDDOC_C", length = 36, nullable = false)
    private String documentId;
    
    /**
     * User ID.
     */
    @Column(name = "COM_IDUSER_C", length = 36, nullable = false)
    private String userId;
    
    /**
     * Content.
     */
    @Column(name = "COM_CONTENT_C", nullable = false)
    private String content;
    
    /**
     * Creation date.
     */
    @Column(name = "COM_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "COM_DELETEDATE_D")
    private Date deleteDate;
    
    /**
     * Getter of id.
     *
     * @return the id
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
     * Getter of documentId.
     *
     * @return the documentId
     */
    public String getDocumentId() {
        return documentId;
    }

    /**
     * Setter of documentId.
     *
     * @param documentId documentId
     */
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    /**
     * Getter of createDate.
     *
     * @return the createDate
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
     * Getter of deleteDate.
     *
     * @return the deleteDate
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
     * Getter of content.
     *
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * Setter of content.
     *
     * @param content content
     */
    public void setContent(String content) {
        this.content = content;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("documentId", documentId)
                .add("userId", userId)
                .toString();
    }

    @Override
    public String toMessage() {
        return documentId;
    }
}
