package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Objects;
import com.sismics.docs.core.constant.PermType;

/**
 * ACL entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_ACL")
public class Acl {
    /**
     * ACL ID.
     */
    @Id
    @Column(name = "ACL_ID_C", length = 36)
    private String id;
    
    /**
     * ACL permission.
     */
    @Column(name = "ACL_PERM_C", length = 30)
    @Enumerated(EnumType.STRING)
    private PermType perm;

    /**
     * ACL source ID.
     */
    @Column(name = "ACL_SOURCEID_C", length = 36)
    private String sourceId;
    
    /**
     * ACL target ID.
     */
    @Column(name = "ACL_TARGETID_C", length = 36)
    private String targetId;
    
    /**
     * Deletion date.
     */
    @Column(name = "ACL_DELETEDATE_D")
    private Date deleteDate;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PermType getPerm() {
        return perm;
    }

    public void setPerm(PermType perm) {
        this.perm = perm;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
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
                .add("perm", perm)
                .add("sourceId", sourceId)
                .add("targetId", targetId)
                .toString();
    }
}
