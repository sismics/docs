package com.sismics.docs.core.util.authentication;

import com.google.common.collect.Lists;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.util.ClasspathScanner;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User utilities.
 */
public class AuthenticationUtil {
    /**
     * List of authentication handlers scanned in the classpath.
     */
    private static final List<AuthenticationHandler> AUTH_HANDLERS = Lists.newArrayList(
            new ClasspathScanner<AuthenticationHandler>().findClasses(AuthenticationHandler.class, "com.sismics.docs.core.util.authentication")
                    .stream()

                    .map(clazz -> {
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList()));

    /**
     * Authenticate a user.
     *
     * @param username Username
     * @param password Password
     * @return Authenticated user
     */
    public static User authenticate(String username, String password) {
        for (AuthenticationHandler authenticationHandler : AUTH_HANDLERS) {
            User user = authenticationHandler.authenticate(username, password);
            if (user != null) {
                return user;
            }
        }
        return null;
    }
}
