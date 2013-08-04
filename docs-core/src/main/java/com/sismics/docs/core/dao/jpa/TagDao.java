package com.sismics.docs.core.dao.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.docs.core.dao.jpa.dto.TagDto;
import com.sismics.docs.core.dao.jpa.dto.TagStatDto;
import com.sismics.docs.core.model.jpa.DocumentTag;
import com.sismics.docs.core.model.jpa.Tag;
import com.sismics.util.context.ThreadLocalContext;

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
     * Returns the list of all tags.
     * 
     * @return List of tags
     */
    @SuppressWarnings("unchecked")
    public List<Tag> getByUserId(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select t from Tag t where t.userId = :userId and t.deleteDate is null order by t.name");
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    /**
     * Update tags on a document.
     * 
     * @param documentId
     * @param tagIdSet
     */
    public void updateTagList(String documentId, Set<String> tagIdSet) {
        // Delete old tag links
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("delete DocumentTag dt where dt.documentId = :documentId");
        q.setParameter("documentId", documentId);
        q.executeUpdate();
        
        // Create new tag links
        for (String tagId : tagIdSet) {
            DocumentTag documentTag = new DocumentTag();
            documentTag.setId(UUID.randomUUID().toString());
            documentTag.setDocumentId(documentId);
            documentTag.setTagId(tagId);
            em.persist(documentTag);
        }

    }

    /**
     * Returns tag list on a document.
     * @param documentId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<TagDto> getByDocumentId(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select t.TAG_ID_C, t.TAG_NAME_C from T_DOCUMENT_TAG dt ");
        sb.append(" join T_TAG t on t.TAG_ID_C = dt.DOT_IDTAG_C ");
        sb.append(" where dt.DOT_IDDOCUMENT_C = :documentId and t.TAG_DELETEDATE_D is null ");
        sb.append(" order by t.TAG_NAME_C ");
        
        // Perform the query
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("documentId", documentId);
        List<Object[]> l = q.getResultList();
        
        // Assemble results
        List<TagDto> tagDtoList = new ArrayList<TagDto>();
        for (Object[] o : l) {
            int i = 0;
            TagDto tagDto = new TagDto();
            tagDto.setId((String) o[i++]);
            tagDto.setName((String) o[i++]);
            tagDtoList.add(tagDto);
        }
        return tagDtoList;
    }
    
    /**
     * Returns stats on tags.
     * @param documentId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<TagStatDto> getStats(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select t.TAG_ID_C, t.TAG_NAME_C, count(d.DOC_ID_C) ");
        sb.append(" from T_TAG t ");
        sb.append(" left join T_DOCUMENT_TAG dt on t.TAG_ID_C = dt.DOT_IDTAG_C ");
        sb.append(" left join T_DOCUMENT d on d.DOC_ID_C = dt.DOT_IDDOCUMENT_C and d.DOC_DELETEDATE_D is null and d.DOC_IDUSER_C = :userId ");
        sb.append(" where t.TAG_DELETEDATE_D is null ");
        sb.append(" group by t.TAG_ID_C ");
        sb.append(" order by t.TAG_NAME_C ");
        
        // Perform the query
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("userId", userId);
        List<Object[]> l = q.getResultList();
        
        // Assemble results
        List<TagStatDto> tagStatDtoList = new ArrayList<TagStatDto>();
        for (Object[] o : l) {
            int i = 0;
            TagStatDto tagDto = new TagStatDto();
            tagDto.setId((String) o[i++]);
            tagDto.setName((String) o[i++]);
            tagDto.setCount(((Number) o[i++]).intValue());
            tagStatDtoList.add(tagDto);
        }
        return tagStatDtoList;
    }
    
    /**
     * Creates a new tag.
     * 
     * @param tag Tag
     * @return New ID
     * @throws Exception
     */
    public String create(Tag tag) {
        // Create the UUID
        tag.setId(UUID.randomUUID().toString());
        
        // Create the tag
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        tag.setCreateDate(new Date());
        em.persist(tag);
        
        return tag.getId();
    }

    /**
     * Returns a tag by user ID and name.
     * @param userId User ID
     * @param name Name
     * @return Tag
     */
    public Tag getByUserIdAndName(String userId, String name) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select t from Tag t where t.name = :name and t.userId = :userId and t.deleteDate is null");
        q.setParameter("userId", userId);
        q.setParameter("name", name);
        try {
            return (Tag) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Returns a tag by user ID and name.
     * @param userId User ID
     * @param tagId Tag ID
     * @return Tag
     */
    public Tag getByUserIdAndTagId(String userId, String tagId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select t from Tag t where t.id = :tagId and t.userId = :userId and t.deleteDate is null");
        q.setParameter("userId", userId);
        q.setParameter("tagId", tagId);
        try {
            return (Tag) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes a tag.
     * 
     * @param tagId Tag ID
     */
    public void delete(String tagId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the tag
        Query q = em.createQuery("select t from Tag t where t.id = :id and t.deleteDate is null");
        q.setParameter("id", tagId);
        Tag tagDb = (Tag) q.getSingleResult();
        
        // Delete the tag
        Date dateNow = new Date();
        tagDb.setDeleteDate(dateNow);

        // Delete linked data
        q = em.createQuery("delete DocumentTag dt where dt.tagId = :tagId");
        q.setParameter("tagId", tagId);
        q.executeUpdate();
    }
}
