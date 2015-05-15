package com.sismics.docs.core.model.jpa;

import com.google.common.base.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

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
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of longLasted.
     *
     * @return longLasted
     */
    public boolean isLongLasted() {
        return longLasted;
    }

    /**
     * Setter of longLasted.
     *
     * @param longLasted longLasted
     */
    public void setLongLasted(boolean longLasted) {
        this.longLasted = longLasted;
    }
    
    /**
     * Getter of ip.
     * @return ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * Setter of ip.
     * @param ip ip
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Getter of userAgent.
     * @return userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Setter of userAgent.
     * @param userAgent userAgent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Getter of creationDate.
     *
     * @return creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Setter of creationDate.
     *
     * @param creationDate creationDate
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Getter of lastConnectionDate.
     *
     * @return lastConnectionDate
     */
    public Date getLastConnectionDate() {
        return lastConnectionDate;
    }

    /**
     * Setter of lastConnectionDate.
     *
     * @param lastConnectionDate lastConnectionDate
     */
    public void setLastConnectionDate(Date lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", "**hidden**")
                .add("userId", userId)
                .add("ip", ip)
                .add("userAgent", userAgent)
                .add("longLasted", longLasted)
                .toString();
    }
}