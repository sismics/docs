package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.core.constant.RouteStepTransition;
import com.sismics.docs.core.constant.RouteStepType;
import com.sismics.docs.core.dao.jpa.dto.RouteStepDto;
import com.sismics.docs.core.model.jpa.RouteStep;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
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

    /**
     * Get the current route step from a document.
     *
     * @param documentId Document ID
     * @return Current route step
     */
    @SuppressWarnings("unchecked")
    public RouteStepDto getCurrentStep(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select rs.RTP_ID_C, rs.RTP_NAME_C, rs.RTP_TYPE_C, rs.RTP_TRANSITION_C, rs.RTP_COMMENT_C, rs.RTP_IDTARGET_C, rs.RTP_ENDDATE_D");
        sb.append(" from T_ROUTE_STEP rs ");
        sb.append(" join T_ROUTE r on r.RTE_ID_C = rs.RTP_IDROUTE_C ");
        sb.append(" where r.RTE_IDDOCUMENT_C = :documentId and rs.RTP_ENDDATE_D is null ");
        sb.append(" order by rs.RTP_ORDER_N asc ");

        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("documentId", documentId);

        List<Object[]> l = q.getResultList();
        if (l.isEmpty()) {
            return null;
        }
        Object[] o = l.get(0);
        int i = 0;
        RouteStepDto routeStepDto = new RouteStepDto();
        routeStepDto.setId((String) o[i++]);
        routeStepDto.setName((String) o[i++]);
        routeStepDto.setType(RouteStepType.valueOf((String) o[i++]));
        String transition = (String) o[i++];
        routeStepDto.setTransition(transition == null ? null : RouteStepTransition.valueOf(transition));
        routeStepDto.setComment((String) o[i++]);
        routeStepDto.setTargetId((String) o[i++]);
        Timestamp endDateTimestamp = (Timestamp) o[i];
        routeStepDto.setEndDateTimestamp(endDateTimestamp == null ? null : endDateTimestamp.getTime());
        return routeStepDto;
    }

    /**
     * End a route step.
     *
     * @param id Route step ID
     * @param transition Transition
     * @param comment Comment
     * @param validatorUserId Validator user ID
     */
    public void endRouteStep(String id, RouteStepTransition transition, String comment, String validatorUserId) {
        StringBuilder sb = new StringBuilder("update T_ROUTE_STEP r ");
        sb.append(" set r.RTP_ENDDATE_D = :endDate, r.RTP_TRANSITION_C = :transition, r.RTP_COMMENT_C = :comment, r.RTP_IDVALIDATORUSER_C = :validatorUserId ");
        sb.append(" where r.RTP_ID_C = :id");

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("endDate", new Date());
        q.setParameter("transition", transition.name());
        q.setParameter("comment", comment);
        q.setParameter("validatorUserId", validatorUserId);
        q.setParameter("id", id);
        q.executeUpdate();
    }
}
