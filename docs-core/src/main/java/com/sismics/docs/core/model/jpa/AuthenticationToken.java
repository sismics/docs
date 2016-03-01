package com.sismics.docs.core.model.jpa;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * Authentication token entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_AUTHENTICATION_TOKEN")
public class AuthenticationToken {
    /**
     * Token.
     */
    @Id
    @Column(name = "AUT_ID_C", length = 36)
    private String id;

    /**
     * User ID.
     */
    @Column(name = "AUT_IDUSER_C", nullable = false, length = 36)
    private String userId;
    
    /**
     * Login IP.
     */
    @Column(name = "AUT_IP_C", nullable = true, length = 45)
    private String ip;
    
    /**
     * Login user agent.
     */
    @Column(name = "AUT_UA_C", nullable = true, length = 1000)
    private String userAgent;
    
    /**
     * Remember the user next time (long lasted session).
     */
    @Column(name = "AUT_LONGLASTED_B", nullable = false)
    private boolean longLasted;
    
    /**
     * Token creation date.
     */
    @Column(name = "AUT_CREATIONDATE_D", nullable = false)
    private Date creationDate;

    /**
     * Last connection date using this token.
     */
    @Column(name = "AUT_LASTCONNECTIONDATE_D")
    private Date lastConnectionDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isLongLasted() {
        return longLasted;
    }

    public void setLongLasted(boolean longLasted) {
        this.longLasted = longLasted;
    }
    
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastConnectionDate() {
        return lastConnectionDate;
    }

    public void setLastConnectionDate(Date lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", "**hidden**")
                .add("userId", userId)
                .add("ip", ip)
                .add("userAgent", userAgent)
                .add("longLasted", longLasted)
                .toString();
    }
}