package com.sismics.docs.core.util;

import com.sismics.docs.core.model.jpa.User;

/**
 * Utilitaires sur les utilisateurs.
 *
 * @author jtremeaux 
 */
public class UserUtil {
    /**
     * Retourne the user's username.
     * 
     * @param user User
     * @return User name
     */
    public static String getUserName(User user) {
        return user.getUsername();
    }
}
