package com.sismics.util.filter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.jpa.AuthenticationTokenDao;
import com.sismics.docs.core.dao.jpa.GroupDao;
import com.sismics.docs.core.dao.jpa.RoleBaseFunctionDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.jpa.criteria.GroupCriteria;
import com.sismics.docs.core.dao.jpa.dto.GroupDto;
import com.sismics.docs.core.model.jpa.AuthenticationToken;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.security.AnonymousPrincipal;
import com.sismics.security.UserPrincipal;

import jersey.repackaged.com.google.common.collect.Sets;

/**
 * This filter is used to authenticate the user having an active session via an authentication token stored in database.
 * The filter extracts the authentication token stored in a cookie.
 * If the cookie exists and the token is valid, the filter injects a UserPrincipal into a request attribute.
 * If not, the user is anonymous, and the filter injects a AnonymousPrincipal into the request attribute.
 *
 * @author jtremeaux
 */
public class TokenBasedSecurityFilter implements Filter {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TokenBasedSecurityFilter.class);

    /**
     * Name of the cookie used to store the authentication token.
     */
    public static final String COOKIE_NAME = "auth_token";

    /**
     * Name of the attribute containing the principal.
     */
    public static final String PRINCIPAL_ATTRIBUTE = "principal";
    
    /**
     * Lifetime of the authentication token in seconds, since login.
     */
    public static final int TOKEN_LONG_LIFETIME = 3600 * 24 * 365 * 20;
    
    /**
     * Lifetime of the authentication token in seconds, since last connection.
     */
    public static final int TOKEN_SESSION_LIFETIME = 3600 * 24;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // NOP
    }

    @Override
    public void destroy() {
        // NOP
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // Get the value of the client authentication token
        HttpServletRequest request = (HttpServletRequest) req;
        String authToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    authToken = cookie.getValue();
                }
            }
        }
        
        // Get the corresponding server token
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = null;
        if (authToken != null) {
            authenticationToken = authenticationTokenDao.get(authToken);
        }
        
        if (authenticationToken == null) {
            injectAnonymousUser(request);
        } else {
            // Check if the token is still valid
            if (isTokenExpired(authenticationToken)) {
                try {
                    injectAnonymousUser(request);

                    // Destroy the expired token
                    authenticationTokenDao.delete(authToken);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(MessageFormat.format("Error deleting authentication token {0} ", authToken), e);
                    }
                }
            } else {
                // Check if the user is still valid
                UserDao userDao = new UserDao();
                User user = userDao.getById(authenticationToken.getUserId());
                if (user != null && user.getDeleteDate() == null) {
                    injectAuthenticatedUser(request, user);
                } else {
                    injectAnonymousUser(request);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Returns true if the token is expired.
     * 
     * @param authenticationToken Authentication token
     * @return Token expired
     */
    private boolean isTokenExpired(AuthenticationToken authenticationToken) {
        final long now = new Date().getTime();
        final long creationDate = authenticationToken.getCreationDate().getTime();
        if (authenticationToken.isLongLasted()) {
            return now >= creationDate + ((long) TOKEN_LONG_LIFETIME) * 1000L;
        } else {
            long date = authenticationToken.getLastConnectionDate() != null ?
                    authenticationToken.getLastConnectionDate().getTime() : creationDate;
            return now >= date + ((long) TOKEN_SESSION_LIFETIME) * 1000L;
        }
    }

    /**
     * Inject an authenticated user into the request attributes.
     * 
     * @param request HTTP request
     * @param user User to inject
     */
    private void injectAuthenticatedUser(HttpServletRequest request, User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user.getId(), user.getUsername());

        // Add groups
        GroupDao groupDao = new GroupDao();
        Set<String> groupRoleIdSet = new HashSet<>();
        List<GroupDto> groupDtoList = groupDao.findByCriteria(new GroupCriteria()
                .setUserId(user.getId())
                .setRecursive(true), null);
        Set<String> groupIdSet = Sets.newHashSet();
        for (GroupDto groupDto : groupDtoList) {
            groupIdSet.add(groupDto.getId());
            if (groupDto.getRoleId() != null) {
                groupRoleIdSet.add(groupDto.getRoleId());
            }
        }
        userPrincipal.setGroupIdSet(groupIdSet);
        
        // Add base functions
        groupRoleIdSet.add(user.getRoleId());
        RoleBaseFunctionDao userBaseFuction = new RoleBaseFunctionDao();
        Set<String> baseFunctionSet = userBaseFuction.findByRoleId(groupRoleIdSet);
        userPrincipal.setBaseFunctionSet(baseFunctionSet);
        
        // Add email
        userPrincipal.setEmail(user.getEmail());
        
        request.setAttribute(PRINCIPAL_ATTRIBUTE, userPrincipal);
    }

    /**
     * Inject an anonymous user into the request attributes.
     * 
     * @param request HTTP request
     */
    private void injectAnonymousUser(HttpServletRequest request) {
        AnonymousPrincipal anonymousPrincipal = new AnonymousPrincipal();
        anonymousPrincipal.setDateTimeZone(DateTimeZone.forID(Constants.DEFAULT_TIMEZONE_ID));

        request.setAttribute(PRINCIPAL_ATTRIBUTE, anonymousPrincipal);
    }
}
