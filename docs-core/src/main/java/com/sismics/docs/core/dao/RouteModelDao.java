package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.criteria.RouteModelCriteria;
import com.sismics.docs.core.dao.dto.RouteModelDto;
import com.sismics.docs.core.model.jpa.RouteModel;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.SecurityUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.*;

/**
 * Route model DAO.
 * 
 * @author bgamard
 */
public class RouteModelDao {
    /**
     * Creates a new route model.
     *
     * @param routeModel Route model
     * @param userId User ID
     * @return New ID
     */
    public String create(RouteModel routeModel, String userId) {
        // Create the UUID
        routeModel.setId(UUID.randomUUID().toString());

        // Create the route model
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        routeModel.setCreateDate(new Date());
        em.persist(routeModel);

        // Create audit log
        AuditLogUtil.create(routeModel, AuditLogType.CREATE, userId);

        return routeModel.getId();
    }

    /**
     * Update a route model.
     *
     * @param routeModel Route model to update
     * @param userId User ID
     * @return Updated route model
     */
    public RouteModel update(RouteModel routeModel, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the route model
        Query q = em.createQuery("select r from RouteModel r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", routeModel.getId());
        RouteModel routeModelDb = (RouteModel) q.getSingleResult();

        // Update the route model
        routeModelDb.setName(routeModel.getName());
        routeModelDb.setSteps(routeModel.getSteps());

        // Create audit log
        AuditLogUtil.create(routeModelDb, AuditLogType.UPDATE, userId);

        return routeModelDb;
    }

    /**
     * Gets an active route model by its ID.
     *
     * @param id Route model ID
     * @return Route model
     */
    public RouteModel getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from RouteModel r where r.id = :id and r.deleteDate is null");
            q.setParameter("id", id);
            return (RouteModel) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Returns the list of all route models.
     *
     * @return List of route models
     */
    @SuppressWarnings("unchecked")
    public List<RouteModel> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select r from RouteModel r where r.deleteDate is null");
        return q.getResultList();
    }

    /**
     * Deletes a route model.
     *
     * @param id Route model ID
     * @param userId User ID
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the route model
        Query q = em.createQuery("select r from RouteModel r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", id);
        RouteModel routeModelDb = (RouteModel) q.getSingleResult();

        // Delete the route model
        Date dateNow = new Date();
        routeModelDb.setDeleteDate(dateNow);

        // Create audit log
        AuditLogUtil.create(routeModelDb, AuditLogType.DELETE, userId);
    }

    /**
     * Returns the list of all route models.
     *
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of route models
     */
    public List<RouteModelDto> findByCriteria(RouteModelCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        List<String> criteriaList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select rm.RTM_ID_C c0, rm.RTM_NAME_C c1, rm.RTM_CREATEDATE_D c2");
        sb.append(" from T_ROUTE_MODEL rm ");

        // Add search criterias
        if (criteria.getTargetIdList() != null && !SecurityUtil.skipAclCheck(criteria.getTargetIdList())) {
            sb.append(" left join T_ACL a on a.ACL_TARGETID_C in (:targetIdList) and a.ACL_SOURCEID_C = rm.RTM_ID_C and a.ACL_PERM_C = 'READ' and a.ACL_DELETEDATE_D is null ");
            criteriaList.add("a.ACL_ID_C is not null");
            parameterMap.put("targetIdList", criteria.getTargetIdList());
        }

        criteriaList.add("rm.RTM_DELETEDATE_D is null");

        sb.append(" where ");
        sb.append(Joiner.on(" and ").join(criteriaList));

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();

        // Assemble results
        List<RouteModelDto> dtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            RouteModelDto dto = new RouteModelDto();
            dto.setId((String) o[i++]);
            dto.setName((String) o[i++]);
            dto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            dtoList.add(dto);
        }
        return dtoList;
    }
}
