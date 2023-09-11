package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.sismics.util.mime.MimeTypeUtil;

import jakarta.persistence.*;
import java.util.Date;

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
     * Name.
     */
    @Column(name = "FIL_NAME_C", length = 200)
    private String name;

    /**
     * MIME type.
     */
    @Column(name = "FIL_MIMETYPE_C", length = 100)
    private String mimeType;

    /**
     * OCR-ized content.
     */
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
     * Version ID.
     */
    @Column(name = "FIL_IDVERSION_C")
    private String versionId;

    /**
     * Version number (starting at 0).
     */
    @Column(name = "FIL_VERSION_N", nullable = false)
    private Integer version;

    /**
     * True if it's the latest version of the file.
     */
    @Column(name = "FIL_LATESTVERSION_B", nullable = false)
    private boolean latestVersion;

    public static final Long UNKNOWN_SIZE = -1L;

    /**
     * Can be {@link File#UNKNOWN_SIZE} if the size has not been stored in the database when the file has been uploaded
     */
    @Column(name = "FIL_SIZE_N", nullable = false)
    private Long size;

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

    public String getName() {
        return name;
    }

    public File setName(String name) {
        this.name = name;
        return this;
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

    public String getVersionId() {
        return versionId;
    }

    public File setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public File setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public boolean isLatestVersion() {
        return latestVersion;
    }

    public File setLatestVersion(boolean latestVersion) {
        this.latestVersion = latestVersion;
        return this;
    }

    /**
     * Can return {@link File#UNKNOWN_SIZE} if the file size is not stored in the database.
     */
    public Long getSize() {
        return size;
    }

    public File setSize(Long size) {
        this.size = size;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }

    @Override
    public String toMessage() {
        // Attached document ID and name concatenated
        return (documentId == null ? Strings.repeat(" ", 36) : documentId) + name;
    }

    /**
     * Build the full file name.
     *
     * @param def Default name if the file doesn't have one.
     * @return File name
     */
    public String getFullName(String def) {
        if (Strings.isNullOrEmpty(name)) {
            return def + "." + MimeTypeUtil.getFileExtension(mimeType);
        }
        return name;
    }
}
