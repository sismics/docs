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
public class Route {
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
     * Creation date.
     */
    @Column(name = "RTE_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "RTE_DELETEDATE_D")
    private Date deleteDate;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("documentId", documentId)
                .add("createDate", createDate)
                .toString();
    }
}
