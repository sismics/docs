package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;

/**
 * File entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_FILE")
public class File {
    /**
     * File ID.
     */
    @Id
    @Column(name = "FIL_ID_C", length = 36)
    private String id;
    
    /**
     * Document ID.
     */
    @Column(name = "FIL_IDDOC_C", nullable = false, length = 36)
    private String documentId;
    
    /**
     * Document ID.
     */
    @Column(name = "FIL_MIMETYPE_C", length = 100)
    private String mimeType;
    
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
     * Getter of mimeType.
     *
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Setter of mimeType.
     *
     * @param mimeType mimeType
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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
                .toString();
    }
}
