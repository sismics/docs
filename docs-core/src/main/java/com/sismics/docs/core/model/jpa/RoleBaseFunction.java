package com.sismics.docs.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

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

    /**
     * Getter of roleId.
     *
     * @return roleId
     */
    public String getRoleId() {
        return roleId;
    }

    /**
     * Setter of roleId.
     *
     * @param roleId roleId
     */
    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * Getter of baseFunctionId.
     *
     * @return baseFunctionId
     */
    public String getBaseFunctionId() {
        return baseFunctionId;
    }

    /**
     * Setter of baseFunctionId.
     *
     * @param baseFunctionId baseFunctionId
     */
    public void setBaseFunctionId(String baseFunctionId) {
        this.baseFunctionId = baseFunctionId;
    }

    /**
     * Getter of createDate.
     *
     * @return createDate
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * Setter of createDate.
     *
     * @param createDate createDate
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * Getter of deleteDate.
     *
     * @return deleteDate
     */
    public Date getDeleteDate() {
        return deleteDate;
    }

    /**
     * Setter of deleteDate.
     *
     * @param deleteDate deleteDate
     */
    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", roleId)
                .add("baseFunctionId", baseFunctionId)
                .toString();
    }
}
