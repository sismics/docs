package com.sismics.docs.core.dao.jpa.dto;

/**
 * User DTO.
 *
 * @author jtremeaux 
 */
public class UserDto {
    /**
     * User ID.
     */
    private String id;
    
    /**
     * Locale ID.
     */
    private String localeId;
    
    /**
     * Username.
     */
    private String username;
    
    /**
     * Email address.
     */
    private String email;
    
    /**
     * Creation date of this user.
     */
    private Long createTimestamp;

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
     * Getter of localeId.
     *
     * @return localeId
     */
    public String getLocaleId() {
        return localeId;
    }

    /**
     * Setter of localeId.
     *
     * @param localeId localeId
     */
    public void setLocaleId(String localeId) {
        this.localeId = localeId;
    }

    /**
     * Getter of username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter of username.
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Getter of email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter of email.
     *
     * @param email email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter of createTimestamp.
     *
     * @return createTimestamp
     */
    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    /**
     * Setter of createTimestamp.
     *
     * @param createTimestamp createTimestamp
     */
    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }
}
