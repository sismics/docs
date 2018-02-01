package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.Route;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.UUID;

/**
 * Route DAO.
 * 
 * @author bgamard
 */
public class RouteDao {
    /**
     * Creates a new route.
     *
     * @param route Route
     * @param userId User ID
     * @return New ID
     */
    public String create(Route route, String userId) {
        // Create the UUID
        route.setId(UUID.randomUUID().toString());

        // Create the route
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        route.setCreateDate(new Date());
        em.persist(route);

        // Create audit log
        AuditLogUtil.create(route, AuditLogType.CREATE, userId);

        return route.getId();
    }
}
