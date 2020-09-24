package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

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
     * False when the user passed the onboarding.
     */
    @Column(name = "USE_ONBOARDING_B", nullable = false)
    private boolean onboarding;

    /**
     * TOTP secret key.
     */
    @Column(name = "USE_TOTPKEY_C", length = 100)
    private String totpKey;
    
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
    
    /**
     * Disable date.
     */
    @Column(name = "USE_DISABLEDATE_D")
    private Date disableDate;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getRoleId() {
        return roleId;
    }

    public User setRoleId(String roleId) {
        this.roleId = roleId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public User setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    @Override
    public Date getDeleteDate() {
        return deleteDate;
    }

    public User setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    public Date getDisableDate() {
        return disableDate;
    }

    public User setDisableDate(Date disableDate) {
        this.disableDate = disableDate;
        return this;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public User setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public Long getStorageQuota() {
        return storageQuota;
    }

    public User setStorageQuota(Long storageQuota) {
        this.storageQuota = storageQuota;
        return this;
    }

    public Long getStorageCurrent() {
        return storageCurrent;
    }

    public User setStorageCurrent(Long storageCurrent) {
        this.storageCurrent = storageCurrent;
        return this;
    }
    
    public String getTotpKey() {
        return totpKey;
    }

    public User setTotpKey(String totpKey) {
        this.totpKey = totpKey;
        return this;
    }

    public boolean isOnboarding() {
        return onboarding;
    }

    public User setOnboarding(boolean onboarding) {
        this.onboarding = onboarding;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .toString();
    }

    @Override
    public String toMessage() {
        return username;
    }
}
