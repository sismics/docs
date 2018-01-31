package com.sismics.docs.core.util;

import com.sismics.docs.core.dao.jpa.RouteDao;
import com.sismics.docs.core.dao.jpa.RouteStepDao;
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
}
