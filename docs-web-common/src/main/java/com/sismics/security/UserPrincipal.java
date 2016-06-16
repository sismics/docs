package com.sismics.security;

import java.util.Set;

import com.sismics.docs.core.constant.Constants;
import org.joda.time.DateTimeZone;

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
     * User groups.
     */
    private Set<String> groupIdSet;
    
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

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DateTimeZone getDateTimeZone() {
        return dateTimeZone;
    }

    public void setDateTimeZone(DateTimeZone dateTimeZone) {
        this.dateTimeZone = dateTimeZone;
    }

    @Override
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getBaseFunctionSet() {
        return baseFunctionSet;
    }

    public void setBaseFunctionSet(Set<String> baseFunctionSet) {
        this.baseFunctionSet = baseFunctionSet;
    }

    @Override
    public Set<String> getGroupIdSet() {
        return groupIdSet;
    }
    
    public void setGroupIdSet(Set<String> groupIdSet) {
        this.groupIdSet = groupIdSet;
    }

    @Override
    public boolean isGuest() {
        return Constants.GUEST_USER_ID.equals(id);
    }
}
