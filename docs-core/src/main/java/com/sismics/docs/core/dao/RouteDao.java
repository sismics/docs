package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.criteria.RouteCriteria;
import com.sismics.docs.core.dao.dto.RouteDto;
import com.sismics.docs.core.model.jpa.Route;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.*;

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

    /**
     * Returns the list of all routes.
     *
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of routes
     */
    public List<RouteDto> findByCriteria(RouteCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        List<String> criteriaList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select r.RTE_ID_C c0, r.RTE_NAME_C c1, r.RTE_CREATEDATE_D c2");
        sb.append(" from T_ROUTE r ");

        // Add search criterias
        if (criteria.getDocumentId() != null) {
            criteriaList.add("r.RTE_IDDOCUMENT_C = :documentId");
            parameterMap.put("documentId", criteria.getDocumentId());
        }
        criteriaList.add("r.RTE_DELETEDATE_D is null");

        sb.append(" where ");
        sb.append(Joiner.on(" and ").join(criteriaList));

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();

        // Assemble results
        List<RouteDto> dtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            RouteDto dto = new RouteDto();
            dto.setId((String) o[i++]);
            dto.setName((String) o[i++]);
            dto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            dtoList.add(dto);
        }
        return dtoList;
    }

    /**
     * Deletes a route and the associated steps.
     *
     * @param routeId Route ID
     * @param userId User ID
     */
    public void deleteRoute(String routeId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Create audit log
        Route route = em.find(Route.class, routeId);
        AuditLogUtil.create(route, AuditLogType.DELETE, userId);

        em.createNativeQuery("update T_ROUTE_STEP rs set RTP_DELETEDATE_D = :dateNow where rs.RTP_IDROUTE_C = :routeId and rs.RTP_DELETEDATE_D is null")
                .setParameter("routeId", routeId)
                .setParameter("dateNow", new Date())
                .executeUpdate();

        em.createNativeQuery("update T_ROUTE r set RTE_DELETEDATE_D = :dateNow where r.RTE_ID_C = :routeId and r.RTE_DELETEDATE_D is null")
                .setParameter("routeId", routeId)
                .setParameter("dateNow", new Date())
                .executeUpdate();
    }
}
