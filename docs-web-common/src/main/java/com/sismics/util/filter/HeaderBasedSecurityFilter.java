package com.sismics.util.filter;

import com.google.common.base.Strings;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.User;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

/**
 * A header-based security filter that authenticates an user using the "X-Authenticated-User" request header as the user ID.
 * This filter is intended to be used in conjunction with an external authenticating proxy.
 *
 * @author pacien
 */
public class HeaderBasedSecurityFilter extends SecurityFilter {
    /**
     * Authentication header.
     */
    public static final String AUTHENTICATED_USER_HEADER = "X-Authenticated-User";

    /**
     * True if this authentication method is enabled.
     */
    private boolean enabled;

    @Override
    public void init(FilterConfig filterConfig) {
        enabled = Boolean.parseBoolean(filterConfig.getInitParameter("enabled"))
                || Boolean.parseBoolean(System.getProperty("docs.header_authentication"));
    }

    @Override
    protected User authenticate(HttpServletRequest request) {
        if (!enabled) {
            return null;
        }

        String username = request.getHeader(AUTHENTICATED_USER_HEADER);
        if (Strings.isNullOrEmpty(username)) {
            return null;
        }
        return new UserDao().getActiveByUsername(username);
    }
}