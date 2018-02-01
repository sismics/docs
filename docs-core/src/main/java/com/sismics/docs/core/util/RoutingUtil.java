package com.sismics.docs.core.util;

import com.sismics.docs.core.constant.AclType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.RouteDao;
import com.sismics.docs.core.dao.jpa.RouteStepDao;
import com.sismics.docs.core.model.jpa.Acl;
import com.sismics.docs.core.model.jpa.Route;
import com.sismics.docs.core.model.jpa.RouteStep;

import java.util.List;

/**
 * Routing utilities.
 *
 * @author bgamard
 */
public class RoutingUtil {
    /**
     * Get the current route step from a document.
     *
     * @param documentId Document ID
     * @return Active route step
     */
    public static RouteStep getCurrentStep(String documentId) {
        // TODO Optimize
        RouteDao routeDao = new RouteDao();
        List<Route> routeList = routeDao.getActiveRoutes(documentId);
        if (routeList.isEmpty()) {
            return null;
        }

        Route route = routeList.get(0);
        RouteStepDao routeStepDao = new RouteStepDao();
        List<RouteStep> routeStepList = routeStepDao.getRouteSteps(route.getId());
        for (RouteStep routeStep : routeStepList) {
            if (routeStep.getEndDate() == null) {
                return routeStep;
            }
        }

        return null;
    }

    /**
     * Update routing ACLs according to the current route step.
     *
     * @param sourceId Source ID
     * @param currentStep Current route step
     * @param previousStep Previous route step
     * @param userId User ID
     */
    public static void updateAcl(String sourceId, RouteStep currentStep, RouteStep previousStep, String userId) {
        AclDao aclDao = new AclDao();

        if (previousStep != null) {
            // Remove the previous ACL
            aclDao.delete(sourceId, PermType.READ, previousStep.getTargetId(), userId, AclType.ROUTING);
        }

        // Create a temporary READ ACL
        Acl acl = new Acl();
        acl.setPerm(PermType.READ);
        acl.setType(AclType.ROUTING);
        acl.setSourceId(sourceId);
        acl.setTargetId(currentStep.getTargetId());
        aclDao.create(acl, userId);
    }
}
