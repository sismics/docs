package com.sismics.docs.core.model.jpa;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Relation entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_RELATION")
public class Relation {
    /**
     * Relation ID.
     */
    @Id
    @Column(name = "REL_ID_C", length = 36)
    private String id;
    
    /**
     * Source document ID.
     */
    @Column(name = "REL_IDDOCFROM_C", length = 36, nullable = false)
    private String fromDocumentId;
    
    /**
     * Destination document ID.
     */
    @Column(name = "REL_IDDOCTO_C", length = 36, nullable = false)
    private String toDocumentId;

    /**
     * Deletion date.
     */
    @Column(name = "REL_DELETEDATE_D")
    private Date deleteDate;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromDocumentId() {
        return fromDocumentId;
    }

    public void setFromDocumentId(String fromDocumentId) {
        this.fromDocumentId = fromDocumentId;
    }

    public String getToDocumentId() {
        return toDocumentId;
    }

    public void setToDocumentId(String toDocumentId) {
        this.toDocumentId = toDocumentId;
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
                .add("fromDocumentId", fromDocumentId)
                .add("toDocumentId", toDocumentId)
                .toString();
    }
}
