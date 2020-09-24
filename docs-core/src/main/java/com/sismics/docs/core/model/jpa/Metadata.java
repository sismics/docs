package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.constant.MetadataType;

import javax.persistence.*;
import java.util.Date;

/**
 * Metadata entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_METADATA")
public class Metadata implements Loggable {
    /**
     * Metadata ID.
     */
    @Id
    @Column(name = "MET_ID_C", length = 36)
    private String id;
    
    /**
     * Name.
     */
    @Column(name = "MET_NAME_C", length = 50, nullable = false)
    private String name;

    /**
     * Type.
     */
    @Column(name = "MET_TYPE_C", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private MetadataType type;

    /**
     * Deletion date.
     */
    @Column(name = "MET_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public Metadata setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Metadata setName(String name) {
        this.name = name;
        return this;
    }

    public MetadataType getType() {
        return type;
    }

    public Metadata setType(MetadataType type) {
        this.type = type;
        return this;
    }

    @Override
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
                .add("name", name)
                .add("type", type)
                .toString();
    }

    @Override
    public String toMessage() {
        return name;
    }
}
