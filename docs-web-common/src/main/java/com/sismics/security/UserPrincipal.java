package com.sismics.security;

import org.joda.time.DateTimeZone;

import java.util.Locale;
import java.util.Set;

/**
 * Authenticated users principal.
 * 
 * @author jtremeaux
 */
public class UserPrincipal implements IPrincipal {
    /**
     * ID of the user.
     */
    private String id;
    
    /**
     * Username of the user.
     */
    private String name;
    
    /**
     * Locale of the principal.
     */
    private Locale locale;
    
    /**
     * Timezone of the principal.
     */
    private DateTimeZone dateTimeZone;
    
    /**
     * Email of the principal.
     */
    private String email;
    
    /**
     * User base functions.
     */
    private Set<String> baseFunctionSet;
    
    /**
     * Constructor of UserPrincipal.
     * 
     * @param id ID of the user
     * @param name Usrename of the user
     */
    public UserPrincipal(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
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

    @Override
    public String getName() {
        return name;
    }

    /**
     * Setter of name.
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Setter of locale.
     *
     * @param locale locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public DateTimeZone getDateTimeZone() {
        return dateTimeZone;
    }

    /**
     * Setter of dateTimeZone.
     *
     * @param dateTimeZone dateTimeZone
     */
    public void setDateTimeZone(DateTimeZone dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }

    @Override
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
     * Getter of baseFunctionSet.
     *
     * @return baseFunctionSet
     */
    public Set<String> getBaseFunctionSet() {
        return baseFunctionSet;
    }

    /**
     * Setter of baseFunctionSet.
     *
     * @param baseFunctionSet baseFunctionSet
     */
    public void setBaseFunctionSet(Set<String> baseFunctionSet) {
        this.baseFunctionSet = baseFunctionSet;
    }

}
