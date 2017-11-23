package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Password recovery entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_PASSWORD_RECOVERY")
public class PasswordRecovery {
    /**
     * Identifier.
     */
    @Id
    @Column(name = "PWR_ID_C", length = 36)
    private String id;
    
    /**
     * Username.
     */
    @Column(name = "PWR_USERNAME_C", nullable = false, length = 50)
    private String username;
    
    /**
     * Creation date.
     */
    @Column(name = "PWR_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Delete date.
     */
    @Column(name = "PWR_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
                .toString();
    }
}
