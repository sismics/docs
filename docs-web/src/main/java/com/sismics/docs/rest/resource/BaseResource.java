package com.sismics.docs.rest.resource;

import com.google.common.collect.Lists;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.security.AnonymousPrincipal;
import com.sismics.security.IPrincipal;
import com.sismics.security.UserPrincipal;
import com.sismics.util.filter.SecurityFilter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.security.Principal;
import java.util.List;
import java.util.Set;

/**
 * Base class of REST resources.
 * 
 * @author jtremeaux
 */
public abstract class BaseResource {
    /**
     * @apiDefine admin Admin
     * Only the admin user can access this resource
     */

    /**
     * @apiDefine user Authenticated user
     * All authenticated users can access this resource
     */

    /**
     * @apiDefine none Anonymous user
     * This resource can be accessed anonymously
     */

    /**
     * @apiDefine server Server error
     */

    /**
     * @apiDefine client Client error
     */

    /**
     * Injects the HTTP request.
     */
    @Context
    protected HttpServletRequest request;
    
    /**
     * Application key.
     */
    @QueryParam("app_key")
    protected String appKey;
    
    /**
     * Principal of the authenticated user.
     */
    protected IPrincipal principal;

    /**
     * This method is used to get the principal of the current
     * user.
     *
     * @return The principal of the logged in user or an anonymous
     * principal.
     */
    protected IPrincipal getPrincipal() {
        Principal principal = (Principal) request.getAttribute(SecurityFilter.PRINCIPAL_ATTRIBUTE);
        if (principal instanceof IPrincipal) {
            IPrincipal iPrincipal = (IPrincipal) principal;
            if(!iPrincipal.isAnonymous()) {
                return iPrincipal;
            }
        }
        return new AnonymousPrincipal();
    }

    /**
     * This method is used to check if the user is authenticated.
     *
     * @throws ForbiddenClientException if the user is not authenticated.
     */
    protected static void authenticate(IPrincipal principal) throws ForbiddenClientException {
        if(!principal.isAnonymous()){
            return;
        }
        throw new ForbiddenClientException();
    }
    
    /**
     * Checks if the user has a base function. Throw an exception if the check fails.
     * 
     * @param baseFunction Base function to check
     */
    void checkBaseFunction(BaseFunction baseFunction) {
        if (!hasBaseFunction(baseFunction)) {
            throw new ForbiddenClientException();
        }
    }
    
    /**
     * Checks if the user has a base function.
     * 
     * @param baseFunction Base function to check
     * @return True if the user has the base function
     */
    boolean hasBaseFunction(BaseFunction baseFunction) {
        if (!(principal instanceof UserPrincipal)) {
            return false;
        }
        Set<String> baseFunctionSet = ((UserPrincipal) principal).getBaseFunctionSet();
        return baseFunctionSet != null && baseFunctionSet.contains(baseFunction.name());
    }
    
    /**
     * Returns a list of ACL target ID.
     * 
     * @param shareId Share ID (optional)
     * @return List of ACL target ID
     */
    List<String> getTargetIdList(String shareId) {
        List<String> targetIdList = Lists.newArrayList(principal.getGroupIdSet());
        if (principal.getId() != null) {
            targetIdList.add(principal.getId());
        }
        if (shareId != null) {
            targetIdList.add(shareId);
        }
        return targetIdList;
    }
}
