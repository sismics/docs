package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.core.model.jpa.RouteStep;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Route step DAO.
 * 
 * @author bgamard
 */
public class RouteStepDao {
    /**
     * Creates a new route step.
     *
     * @param routeStep Route step
     * @return New ID
     */
    public String create(RouteStep routeStep) {
        // Create the UUID
        routeStep.setId(UUID.randomUUID().toString());

        // Create the route step
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        routeStep.setCreateDate(new Date());
        em.persist(routeStep);

        return routeStep.getId();
    }

    @SuppressWarnings("unchecked")
    public List<RouteStep> getRouteSteps(String routeId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("from RouteStep r where r.routeId = :routeId and r.deleteDate is null order by r.order asc");
        q.setParameter("routeId", routeId);
        return q.getResultList();
    }
}
