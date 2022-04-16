package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.constant.MetadataType;
import com.sismics.docs.core.dao.criteria.MetadataCriteria;
import com.sismics.docs.core.dao.dto.MetadataDto;
import com.sismics.docs.core.model.jpa.Metadata;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

/**
 * Metadata DAO.
 * 
 * @author bgamard
 */
public class MetadataDao {
    /**
     * Creates a new metdata.
     *
     * @param metadata Metadata
     * @param userId User ID
     * @return New ID
     */
    public String create(Metadata metadata, String userId) {
        // Create the UUID
        metadata.setId(UUID.randomUUID().toString());

        // Create the metadata
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(metadata);

        // Create audit log
        AuditLogUtil.create(metadata, AuditLogType.CREATE, userId);

        return metadata.getId();
    }

    /**
     * Update a metadata.
     *
     * @param metadata Metadata to update
     * @param userId User ID
     * @return Updated metadata
     */
    public Metadata update(Metadata metadata, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the metadata
        Query q = em.createQuery("select r from Metadata r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", metadata.getId());
        Metadata metadataDb = (Metadata) q.getSingleResult();

        // Update the metadata
        metadataDb.setName(metadata.getName());

        // Create audit log
        AuditLogUtil.create(metadataDb, AuditLogType.UPDATE, userId);

        return metadataDb;
    }

    /**
     * Gets an active metadata by its ID.
     *
     * @param id Metadata ID
     * @return Metadata
     */
    public Metadata getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from Metadata r where r.id = :id and r.deleteDate is null");
            q.setParameter("id", id);
            return (Metadata) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Deletes a metadata.
     *
     * @param id Metadata ID
     * @param userId User ID
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the metadata
        Query q = em.createQuery("select r from Metadata r where r.id = :id and r.deleteDate is null");
        q.setParameter("id", id);
        Metadata metadataDb = (Metadata) q.getSingleResult();

        // Delete the metadata
        Date dateNow = new Date();
        metadataDb.setDeleteDate(dateNow);

        // Create audit log
        AuditLogUtil.create(metadataDb, AuditLogType.DELETE, userId);
    }

    /**
     * Returns the list of all metadata.
     *
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of metadata
     */
    public List<MetadataDto> findByCriteria(MetadataCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select m.MET_ID_C c0, m.MET_NAME_C c1, m.MET_TYPE_C c2");
        sb.append(" from T_METADATA m ");

        criteriaList.add("m.MET_DELETEDATE_D is null");

        sb.append(" where ");
        sb.append(Joiner.on(" and ").join(criteriaList));

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();

        // Assemble results
        List<MetadataDto> dtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            MetadataDto dto = new MetadataDto();
            dto.setId((String) o[i++]);
            dto.setName((String) o[i++]);
            dto.setType(MetadataType.valueOf((String) o[i]));
            dtoList.add(dto);
        }
        return dtoList;
    }
}
