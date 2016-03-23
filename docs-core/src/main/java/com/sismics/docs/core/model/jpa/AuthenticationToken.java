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

    public AuthenticationToken setId(String id) {
        this.id = id;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public AuthenticationToken setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public boolean isLongLasted() {
        return longLasted;
    }

    public AuthenticationToken setLongLasted(boolean longLasted) {
        this.longLasted = longLasted;
        return this;
    }
    
    public String getIp() {
        return ip;
    }

    public AuthenticationToken setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public AuthenticationToken setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public AuthenticationToken setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public Date getLastConnectionDate() {
        return lastConnectionDate;
    }

    public AuthenticationToken setLastConnectionDate(Date lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
        return this;
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