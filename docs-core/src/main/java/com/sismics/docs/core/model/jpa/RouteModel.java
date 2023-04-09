package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * Route model.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_ROUTE_MODEL")
public class RouteModel implements Loggable {
    /**
     * Route model ID.
     */
    @Id
    @Column(name = "RTM_ID_C", length = 36)
    private String id;
    
    /**
     * Name.
     */
    @Column(name = "RTM_NAME_C", nullable = false, length = 50)
    private String name;

    /**
     * Data.
     */
    @Column(name = "RTM_STEPS_C", nullable = false, length = 5000)
    private String steps;

    /**
     * Creation date.
     */
    @Column(name = "RTM_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Deletion date.
     */
    @Column(name = "RTM_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public RouteModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public RouteModel setName(String name) {
        this.name = name;
        return this;
    }

    public String getSteps() {
        return steps;
    }

    public RouteModel setSteps(String steps) {
        this.steps = steps;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public RouteModel setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public RouteModel setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
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
        return name;
    }
}
