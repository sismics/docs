package com.sismics.security;

import org.joda.time.DateTimeZone;

/**
 * Anonymous principal.
 * 
 * @author jtremeaux
 */
public class AnonymousPrincipal implements IPrincipal {
    public static final String ANONYMOUS = "anonymous";
    
    /**
     * User timezone.
     */
    private DateTimeZone dateTimeZone;
    
    /**
     * Constructor of AnonymousPrincipal.
     */
    public AnonymousPrincipal() {
        // NOP
    }
    
    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getName() {
        return ANONYMOUS;
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public DateTimeZone getDateTimeZone() {
        return dateTimeZone;
    }

    @Override
    public String getEmail() {
        return null;
    }
    
    /**
     * Setter of dateTimeZone.
     *
     * @param dateTimeZone dateTimeZone
     */
    public void setDateTimeZone(DateTimeZone dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }
}
