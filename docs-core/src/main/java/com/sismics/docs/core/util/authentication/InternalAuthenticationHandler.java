package com.sismics.docs.core.util.authentication;

import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.util.ClasspathScanner;

/**
 * Authenticate using the internal database.
 *
 * @author bgamard
 */
@ClasspathScanner.Priority(100) // We can add handlers before this one
public class InternalAuthenticationHandler implements AuthenticationHandler {
    @Override
    public User authenticate(String username, String password) {
        UserDao userDao = new UserDao();
        return userDao.authenticate(username, password);
    }
}
