package com.sismics.docs.core.util.authentication;

import com.sismics.docs.core.model.jpa.User;

/**
 * An authentication handler.
 *
 * @author bgamard
 */
public interface AuthenticationHandler {
    /**
     * Authenticate a user.
     *
     * @param username Username
     * @param password Password
     * @return Authenticated user
     */
    User authenticate(String username, String password);
}
