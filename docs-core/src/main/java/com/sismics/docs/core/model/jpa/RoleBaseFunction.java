package com.sismics.docs.core.model.jpa;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Role base function.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_ROLE_BASE_FUNCTION")
public class RoleBaseFunction {
    /**
     * Role base function ID.
     */
    @Id
    @Column(name = "RBF_ID_C", length = 36)
    private String id;
    
    /**
     * Role ID.
     */
    @Column(name = "RBF_IDROLE_C", nullable = false, length = 36)
    private String roleId;
    
    /**
     * Base function ID.
     */
    @Column(name = "RBF_IDBASEFUNCTION_C", nullable = false, length = 36)
    private String baseFunctionId;
    
    /**
     * Creation date.
     */
    @Column(name = "RBF_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "RBF_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getBaseFunctionId() {
        return baseFunctionId;
    }

    public void setBaseFunctionId(String baseFunctionId) {
        this.baseFunctionId = baseFunctionId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
                .add("userId", roleId)
                .add("baseFunctionId", baseFunctionId)
                .toString();
    }
}
