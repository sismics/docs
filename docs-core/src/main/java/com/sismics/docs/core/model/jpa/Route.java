package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Route.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_ROUTE")
public class Route implements Loggable {
    /**
     * Route ID.
     */
    @Id
    @Column(name = "RTE_ID_C", length = 36)
    private String id;

    /**
     * Document ID.
     */
    @Column(name = "RTE_IDDOCUMENT_C", nullable = false, length = 36)
    private String documentId;

    /**
     * Name.
     */
    @Column(name = "RTE_NAME_C", nullable = false, length = 50)
    private String name;

    /**
     * Creation date.
     */
    @Column(name = "RTE_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "RTE_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public Route setId(String id) {
        this.id = id;
        return this;
    }

    public String getDocumentId() {
        return documentId;
    }

    public Route setDocumentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getName() {
        return name;
    }

    public Route setName(String name) {
        this.name = name;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Route setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public Route setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    @Override
    public String toMessage() {
        return documentId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("documentId", documentId)
                .add("createDate", createDate)
                .toString();
    }
}
