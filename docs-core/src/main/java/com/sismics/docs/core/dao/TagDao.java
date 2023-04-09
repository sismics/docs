package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.dto.TagDto;
import com.sismics.docs.core.model.jpa.DocumentTag;
import com.sismics.docs.core.model.jpa.Tag;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.docs.core.util.SecurityUtil;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.QueryUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.*;

/**
 * Tag DAO.
 * 
 * @author bgamard
 */
public class TagDao {
    /**
     * Gets a tag by its ID.
     * 
     * @param id Tag ID
     * @return Tag
     */
    public Tag getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(Tag.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Update tags on a document.
     * 
     * @param documentId Document ID
     * @param tagIdSet Set of tag ID
     */
    public void updateTagList(String documentId, Set<String> tagIdSet) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get current tag links
        Query q = em.createQuery("select dt from DocumentTag dt where dt.documentId = :documentId and dt.deleteDate is null");
        q.setParameter("documentId", documentId);
        @SuppressWarnings("unchecked")
        List<DocumentTag> documentTagList = q.getResultList();
        
        // Deleting tags no longer linked
        for (DocumentTag documentTag : documentTagList) {
            if (!tagIdSet.contains(documentTag.getTagId())) {
                documentTag.setDeleteDate(new Date());
            }
        }
        
        // Adding new tag links
        for (String tagId : tagIdSet) {
            boolean found = false;
            for (DocumentTag documentTag : documentTagList) {
                if (documentTag.getTagId().equals(tagId)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                DocumentTag documentTag = new DocumentTag();
                documentTag.setId(UUID.randomUUID().toString());
                documentTag.setDocumentId(documentId);
                documentTag.setTagId(tagId);
                em.persist(documentTag);
            }
        }
    }
    
    /**
     * Creates a new tag.
     * 
     * @param tag Tag
     * @param userId User ID
     * @return New ID
     */
    public String create(Tag tag, String userId) {
        // Create the UUID
        tag.setId(UUID.randomUUID().toString());
        
        // Create the tag
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        tag.setCreateDate(new Date());
        em.persist(tag);
        
        // Create audit log
        AuditLogUtil.create(tag, AuditLogType.CREATE, userId);
        
        return tag.getId();
    }
    
    /**
     * Deletes a tag.
     * 
     * @param tagId Tag ID
     * @param userId User ID
     */
    public void delete(String tagId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the tag
        Query q = em.createQuery("select t from Tag t where t.id = :id and t.deleteDate is null");
        q.setParameter("id", tagId);
        Tag tagDb = (Tag) q.getSingleResult();
        
        // Delete the tag
        Date dateNow = new Date();
        tagDb.setDeleteDate(dateNow);

        // Delete linked data
        q = em.createQuery("update DocumentTag dt set dt.deleteDate = :dateNow where dt.tagId = :tagId and dt.deleteDate is not null");
        q.setParameter("dateNow", dateNow);
        q.setParameter("tagId", tagId);
        q.executeUpdate();

        q = em.createQuery("update Acl a set a.deleteDate = :dateNow where a.sourceId = :tagId and a.deleteDate is null");
        q.setParameter("tagId", tagId);
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();

        q = em.createQuery("update Tag t set t.parentId = null where t.parentId = :tagId and t.deleteDate is null");
        q.setParameter("tagId", tagId);
        q.executeUpdate();
        
        // Create audit log
        AuditLogUtil.create(tagDb, AuditLogType.DELETE, userId);
    }
    
    /**
     * Update a tag.
     * 
     * @param tag Tag to update
     * @param userId User ID
     * @return Updated tag
     */
    public Tag update(Tag tag, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the tag
        Query q = em.createQuery("select t from Tag t where t.id = :id and t.deleteDate is null");
        q.setParameter("id", tag.getId());
        Tag tagDb = (Tag) q.getSingleResult();
        
        // Update the tag
        tagDb.setName(tag.getName());
        tagDb.setColor(tag.getColor());
        tagDb.setParentId(tag.getParentId());
        
        // Create audit log
        AuditLogUtil.create(tagDb, AuditLogType.UPDATE, userId);
        
        return tagDb;
    }

    /**
     * Returns the list of all tags.
     *
     * @param criteria Search criteria
     * @param sortCriteria Sort criteria
     * @return List of groups
     */
    public List<TagDto> findByCriteria(TagCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        List<String> criteriaList = new ArrayList<>();

        StringBuilder sb = new StringBuilder("select distinct t.TAG_ID_C as c0, t.TAG_NAME_C as c1, t.TAG_COLOR_C as c2, t.TAG_IDPARENT_C as c3, u.USE_USERNAME_C as c4 ");
        sb.append(" from T_TAG t ");
        sb.append(" join T_USER u on t.TAG_IDUSER_C = u.USE_ID_C ");

        // Add search criterias
        if (criteria.getId() != null) {
            criteriaList.add("t.TAG_ID_C = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getTargetIdList() != null && !SecurityUtil.skipAclCheck(criteria.getTargetIdList())) {
            sb.append(" left join T_ACL a on a.ACL_TARGETID_C in (:targetIdList) and a.ACL_SOURCEID_C = t.TAG_ID_C and a.ACL_PERM_C = 'READ' and a.ACL_DELETEDATE_D is null ");
            criteriaList.add("a.ACL_ID_C is not null");
            parameterMap.put("targetIdList", criteria.getTargetIdList());
        }
        if (criteria.getDocumentId() != null) {
            sb.append(" join T_DOCUMENT_TAG dt on dt.DOT_IDTAG_C = t.TAG_ID_C and dt.DOT_DELETEDATE_D is null ");
            criteriaList.add("dt.DOT_IDDOCUMENT_C = :documentId");
            parameterMap.put("documentId", criteria.getDocumentId());
        }

        criteriaList.add("t.TAG_DELETEDATE_D is null");

        sb.append(" where ");
        sb.append(Joiner.on(" and ").join(criteriaList));

        // Perform the search
        QueryParam queryParam = QueryUtil.getSortedQueryParam(new QueryParam(sb.toString(), parameterMap), sortCriteria);
        @SuppressWarnings("unchecked")
        List<Object[]> l = QueryUtil.getNativeQuery(queryParam).getResultList();

        // Assemble results
        List<TagDto> tagDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            TagDto tagDto = new TagDto()
                    .setId((String) o[i++])
                    .setName((String) o[i++])
                    .setColor((String) o[i++])
                    .setParentId((String) o[i++])
                    .setCreator((String) o[i]);
            tagDtoList.add(tagDto);
        }

        return tagDtoList;
    }
}

