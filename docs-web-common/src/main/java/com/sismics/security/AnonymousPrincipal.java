package com.sismics.security;

import java.util.Locale;

import org.joda.time.DateTimeZone;

/**
 * Anonymous principal.
 * 
 * @author jtremeaux
 */
public class AnonymousPrincipal implements IPrincipal {
    public static final String ANONYMOUS = "anonymous";
    
    /**
     * User locale.
     */
    private Locale locale;
    
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
