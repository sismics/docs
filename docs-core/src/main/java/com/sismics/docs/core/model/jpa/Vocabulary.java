package com.sismics.docs.core.model.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Vocabulary entry entity.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_VOCABULARY")
public class Vocabulary {
    /**
     * Vocabulary ID.
     */
    @Id
    @Column(name = "VOC_ID_C", nullable = false, length = 36)
    private String id;
    
    /**
     * Vocabulary name.
     */
    @Column(name = "VOC_NAME_C", nullable = false, length = 50)
    private String name;
    
    /**
     * Vocabulary value.
     */
    @Column(name = "VOC_VALUE_C", nullable = false, length = 500)
    private String value;

    /**
     * Vocabulary order.
     */
    @Column(name = "VOC_ORDER_N")
    private int order;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("value", value)
                .add("order", order)
                .toString();
    }
}
