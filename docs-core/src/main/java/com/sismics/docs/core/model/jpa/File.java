package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.google.common.base.MoreObjects;

/**
 * File entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_FILE")
public class File implements Loggable {
    /**
     * File ID.
     */
    @Id
    @Column(name = "FIL_ID_C", length = 36)
    private String id;
    
    /**
     * Document ID.
     */
    @Column(name = "FIL_IDDOC_C", length = 36)
    private String documentId;
    
    /**
     * User ID.
     */
    @Column(name = "FIL_IDUSER_C", length = 36, nullable = false)
    private String userId;
    
    /**
     * MIME type.
     */
    @Column(name = "FIL_MIMETYPE_C", length = 100)
    private String mimeType;
    
    /**
     * OCR-ized content.
     */
    @Lob
    @Column(name = "FIL_CONTENT_C")
    private String content;
    
    /**
     * Creation date.
     */
    @Column(name = "FIL_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "FIL_DELETEDATE_D")
    private Date deleteDate;
    
    /**
     * Display order of this file.
     */
    @Column(name = "FIL_ORDER_N")
    private Integer order;
    
    /**
     * Private key to decrypt the file.
     * Not saved to database, of course.
     */
    @Transient
    private String privateKey;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .toString();
    }

    @Override
    public String toMessage() {
        return documentId;
    }
}
