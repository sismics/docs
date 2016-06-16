package com.sismics.security;

import java.security.Principal;
import java.util.Set;

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
     * Checks if the principal is a guest.
     *
     * @return True if the principal is a guest
     */
    boolean isGuest();

    /**
     * Returns the ID of the connected user, or null if the user is anonymous
     * 
     * @return ID of the connected user
     */
    public String getId();
    
    /**
     * Returns the list of group ID of the connected user,
     * or an empty list if the user is anonymous.
     * 
     * @return List of group ID
     */
    public Set<String> getGroupIdSet();
    
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
