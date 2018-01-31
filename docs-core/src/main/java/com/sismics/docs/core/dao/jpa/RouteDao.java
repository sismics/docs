package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.Route;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
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

    @SuppressWarnings("unchecked")
    public List<Route> getActiveRoutes(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("from Route r where r.documentId = :documentId and r.deleteDate is null order by r.createDate desc");
        q.setParameter("documentId", documentId);
        return q.getResultList();
    }
}
