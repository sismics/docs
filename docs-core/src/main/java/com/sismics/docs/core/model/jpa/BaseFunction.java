package com.sismics.docs.core.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Base function entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_BASE_FUNCTION")
public class BaseFunction {
    /**
     * Base function ID (ex: "ADMIN").
     */
    @Id
    @Column(name = "BAF_ID_C", length = 10)
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}
