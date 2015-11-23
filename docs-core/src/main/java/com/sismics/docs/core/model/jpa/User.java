package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * User entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_USER")
public class User implements Loggable {
    /**
     * User ID.
     */
    @Id
    @Column(name = "USE_ID_C", length = 36)
    private String id;
    
    /**
     * Role ID.
     */
    @Column(name = "USE_IDROLE_C", nullable = false, length = 36)
    private String roleId;
    
    /**
     * User's username.
     */
    @Column(name = "USE_USERNAME_C", nullable = false, length = 50)
    private String username;
    
    /**
     * User's password.
     */
    @Column(name = "USE_PASSWORD_C", nullable = false, length = 100)
    private String password;

    /**
     * User's private key.
     */
    @Column(name = "USE_PRIVATEKEY_C", nullable = false, length = 100)
    private String privateKey;
    
    /**
     * Email address.
     */
    @Column(name = "USE_EMAIL_C", nullable = false, length = 100)
    private String email;
    
    /**
     * Storage quota.
     */
    @Column(name = "USE_STORAGEQUOTA_N", nullable = false)
    private Long storageQuota;
    
    /**
     * Storage current usage.
     */
    @Column(name = "USE_STORAGECURRENT_N", nullable = false)
    private Long storageCurrent;
    
    /**
     * Creation date.
     */
    @Column(name = "USE_CREATEDATE_D", nullable = false)
    private Date createDate;
    
    /**
     * Deletion date.
     */
    @Column(name = "USE_DELETEDATE_D")
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
    }
    
    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public Long getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(Long storageQuota) {
        this.storageQuota = storageQuota;
    }

    public Long getStorageCurrent() {
        return storageCurrent;
    }

    public void setStorageCurrent(Long storageCurrent) {
        this.storageCurrent = storageCurrent;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .toString();
    }

    @Override
    public String toMessage() {
        return username;
    }
}
