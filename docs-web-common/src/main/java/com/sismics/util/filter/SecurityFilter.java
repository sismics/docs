package com.sismics.util.filter;

import com.google.common.collect.Sets;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.GroupDao;
import com.sismics.docs.core.dao.RoleBaseFunctionDao;
import com.sismics.docs.core.dao.criteria.GroupCriteria;
import com.sismics.docs.core.dao.dto.GroupDto;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.security.AnonymousPrincipal;
import com.sismics.security.UserPrincipal;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract security filter for user authentication, that injects corresponding users into the request.
 * Successfully authenticated users are injected as UserPrincipal, or as AnonymousPrincipal otherwise.
 * If an user has already been authenticated for the request, no further authentication attempt is made.
 *
 * @author pacien
 * @author jtremeaux
 */
public abstract class SecurityFilter implements Filter {
    /**
     * Name of the attribute containing the principal.
     */
    public static final String PRINCIPAL_ATTRIBUTE = "principal";

    /**
     * Logger.
     */
    static final Logger LOG = LoggerFactory.getLogger(SecurityFilter.class);

    /**
     * Returns true if the supplied request has an UserPrincipal.
     *
     * @param request HTTP request
     * @return True if the supplied request has an UserPrincipal
     */
    private boolean hasIdentifiedUser(HttpServletRequest request) {
        return request.getAttribute(PRINCIPAL_ATTRIBUTE) instanceof UserPrincipal;
    }

    /**
     * Injects the given user into the request, with the appropriate authentication state.
     *
     * @param request HTTP request
     * @param user nullable User to inject
     */
    private void injectUser(HttpServletRequest request, User user) {
        // Check if the user is still valid
        if (user != null && user.getDeleteDate() == null && user.getDisableDate() == null) {
            injectAuthenticatedUser(request, user);
        } else {
            injectAnonymousUser(request);
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
        RoleBaseFunctionDao userBaseFunction = new RoleBaseFunctionDao();
        Set<String> baseFunctionSet = userBaseFunction.findByRoleId(groupRoleIdSet);
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

    @Override
    public void init(FilterConfig filterConfig) {
        // NOP
    }

    @Override
    public void destroy() {
        // NOP
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        if (!hasIdentifiedUser(request)) {
            User user = authenticate(request);
            injectUser(request, user);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authenticates an user from the given request parameters.
     *
     * @param request HTTP request
     * @return nullable User
     */
    protected abstract User authenticate(HttpServletRequest request);

}