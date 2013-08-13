package com.sismics.docs.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * File share.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_FILESHARE")
public class FileShare {
    /**
     * File share ID.
     */
    @Id
    @Column(name = "FSH_ID_C", length = 36)
    private String id;

    /**
     * File ID.
     */
    @Column(name = "FSH_IDFILE_C", nullable = false, length = 36)
    private String fileId;
    
    /**
     * Creation date.
     */
    @Column(name = "FSH_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "FSH_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
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
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("tagId", fileId)
                .toString();
    }
}
