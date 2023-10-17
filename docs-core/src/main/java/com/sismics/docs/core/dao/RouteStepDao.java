package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.sismics.docs.core.constant.AclTargetType;
import com.sismics.docs.core.constant.RouteStepTransition;
import com.sismics.docs.core.constant.RouteStepType;
import com.sismics.docs.core.dao.criteria.RouteStepCriteria;
import com.sismics.docs.core.dao.dto.RouteStepDto;
import com.sismics.docs.core.model.jpa.RouteStep;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

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
    public RouteStepDto getCurrentStep(String documentId) {
        List<RouteStepDto> routeStepDtoList = findByCriteria(new RouteStepCriteria()
                .setDocumentId(documentId)
                .setEndDateIsNull(true), new SortCriteria(6, true));
        if (routeStepDtoList.isEmpty()) {
            return null;
        }
        return routeStepDtoList.get(0);
    }

    /**
     * Returns the list of all route steps.
     *
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of route steps
     */
    public List<RouteStepDto> findByCriteria(RouteStepCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select rs.RTP_ID_C, rs.RTP_NAME_C c0, rs.RTP_TYPE_C c1, rs.RTP_TRANSITION_C c2, rs.RTP_COMMENT_C c3, rs.RTP_IDTARGET_C c4, u.USE_USERNAME_C as targetUsername, g.GRP_NAME_C, rs.RTP_ENDDATE_D c5, uv.USE_USERNAME_C as validatorUsername, rs.RTP_IDROUTE_C, rs.RTP_TRANSITIONS_C, rs.RTP_ORDER_N c6")
            .append(" from T_ROUTE_STEP rs ")
            .append(" join T_ROUTE r on r.RTE_ID_C = rs.RTP_IDROUTE_C ")
            .append(" left join T_USER uv on uv.USE_ID_C = rs.RTP_IDVALIDATORUSER_C ")
            .append(" left join T_USER u on u.USE_ID_C = rs.RTP_IDTARGET_C ")
            .append(" left join T_SHARE s on s.SHA_ID_C = rs.RTP_IDTARGET_C ")
            .append(" left join T_GROUP g on g.GRP_ID_C = rs.RTP_IDTARGET_C ");

        // Add search criterias
        if (criteria.getDocumentId() != null) {
            criteriaList.add("r.RTE_IDDOCUMENT_C = :documentId");
            parameterMap.put("documentId", criteria.getDocumentId());
        }
        if (criteria.getRouteId() != null) {
            criteriaList.add("rs.RTP_IDROUTE_C = :routeId");
            parameterMap.put("routeId", criteria.getRouteId());
        }
        if (criteria.getEndDateIsNull() != null) {
            criteriaList.add("RTP_ENDDATE_D is " + (criteria.getEndDateIsNull() ? "" : "not") + " null");
        }
        criteriaList.add("rs.RTP_DELETEDATE_D is null");

        sb.append(" where ");
        sb.append(Joiner.on(" and ").join(criteriaList));

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();

        // Assemble results
        List<RouteStepDto> dtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            RouteStepDto dto = new RouteStepDto();
            dto.setId((String) o[i++]);
            dto.setName((String) o[i++]);
            dto.setType(RouteStepType.valueOf((String) o[i++]));
            dto.setTransition((String) o[i++]);
            dto.setComment((String) o[i++]);
            dto.setTargetId((String) o[i++]);
            String userName = (String) o[i++];
            String groupName = (String) o[i++];
            if (userName != null) {
                dto.setTargetName(userName);
                dto.setTargetType(AclTargetType.USER.name());
            }
            if (groupName != null) {
                dto.setTargetName(groupName);
                dto.setTargetType(AclTargetType.GROUP.name());
            }
            Timestamp endDateTimestamp = (Timestamp) o[i++];
            dto.setEndDateTimestamp(endDateTimestamp == null ? null : endDateTimestamp.getTime());
            dto.setValidatorUserName((String) o[i++]);
            dto.setRouteId((String) o[i++]);
            dto.setTransitions((String) o[i]);
            dtoList.add(dto);
        }
        return dtoList;
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
        sb.append(" set RTP_ENDDATE_D = :endDate, RTP_TRANSITION_C = :transition, RTP_COMMENT_C = :comment, RTP_IDVALIDATORUSER_C = :validatorUserId ");
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
