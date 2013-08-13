package com.sismics.docs.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

    /**
     * Getter of id.
     *
     * @return id
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
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .toString();
    }
}
