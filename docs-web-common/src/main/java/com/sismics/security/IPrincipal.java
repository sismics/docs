package com.sismics.security;

import java.security.Principal;
import java.util.Locale;

import org.joda.time.DateTimeZone;

/**
 * Interface of principals.
 * 
 * @author jtremeaux
 */
public interface IPrincipal extends Principal {
    /**
     * Checks if the principal is anonymous.
     * 
     * @return True if the principal is anonymous.
     */
    boolean isAnonymous();

    /**
     * Returns the ID of the connected user, or null if the user is anonymous
     * 
     * @return ID of the connected user
     */
    public String getId();
    
    /**
     * Returns the locale of the principal.
     *
     * @return Locale of the principal
     */
    public Locale getLocale();

    /**
     * Returns the timezone of the principal.
     *
     * @return Timezone of the principal
     */
    public DateTimeZone getDateTimeZone();

    /**
     * Returns the email of the principal.
     *
     * @return Email of the principal
     */
    public String getEmail();
}
