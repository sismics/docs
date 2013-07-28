package com.sismics.docs.core.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.google.common.base.Joiner;
import com.sismics.docs.core.dao.jpa.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Document DAO.
 * 
 * @author bgamard
 */
public class DocumentDao {
    /**
     * Creates a new document.
     * 
     * @param document Document
     * @return New ID
     * @throws Exception
     */
    public String create(Document document) {
        // Create the UUID
        document.setId(UUID.randomUUID().toString());
        
        // Create the document
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        document.setCreateDate(new Date());
        em.persist(document);
        
        return document.getId();
    }
    
    /**
     * Returns an active document.
     * 
     * @param id Document ID
     * @param userId User ID
     * @return Document
     */
    public Document getDocument(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select d from Document d where d.id = :id and d.userId = :userId and d.deleteDate is null");
        q.setParameter("id", id);
        q.setParameter("userId", userId);
        return (Document) q.getSingleResult();
    }
    
    /**
     * Deletes a document.
     * 
     * @param id Document ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the document
        Query q = em.createQuery("select d from Document d where d.id = :id and d.deleteDate is null");
        q.setParameter("id", id);
        Document documentDb = (Document) q.getSingleResult();
        
        // Delete the document
        Date dateNow = new Date();
        documentDb.setDeleteDate(dateNow);

        // Delete linked data
        q = em.createQuery("update File f set f.deleteDate = :dateNow where f.documentId = :documentId and f.deleteDate is null");
        q.setParameter("documentId", documentDb.getId());
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
    }
    
    /**
     * Gets a document by its ID.
     * 
     * @param id Document ID
     * @return Document
     */
    public Document getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(Document.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Searches documents by criteria.
     * 
     * @param paginatedList List of documents (updated by side effects)
     * @param criteria Search criteria
     * @return List of document
     */
    public void findByCriteria(PaginatedList<DocumentDto> paginatedList, DocumentCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        StringBuilder sb = new StringBuilder("select d.DOC_ID_C c0, d.DOC_TITLE_C c1, d.DOC_DESCRIPTION_C c2, d.DOC_CREATEDATE_D c3");
        sb.append(" from T_DOCUMENT d ");
        
        // Adds search criteria
        List<String> criteriaList = new ArrayList<String>();
        
        if (criteria.getUserId() != null) {
            criteriaList.add("d.DOC_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        
        if (criteria.getSearch() != null) {
            criteriaList.add("(d.DOC_TITLE_C LIKE :search OR d.DOC_DESCRIPTION_C LIKE :search)");
            parameterMap.put("search", "%" + criteria.getSearch() + "%");
        }
        
        if (!criteriaList.isEmpty()) {
            sb.append(" where ");
            sb.append(Joiner.on(" and ").join(criteriaList));
        }
        
        // Perform the search
        QueryParam queryParam = new QueryParam(sb.toString(), parameterMap);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        
        // Assemble results
        List<DocumentDto> documentDtoList = new ArrayList<DocumentDto>();
        for (Object[] o : l) {
            int i = 0;
            DocumentDto documentDto = new DocumentDto();
            documentDto.setId((String) o[i++]);
            documentDto.setTitle((String) o[i++]);
            documentDto.setDescription((String) o[i++]);
            documentDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            documentDtoList.add(documentDto);
        }

        paginatedList.setResultList(documentDtoList);
    }
}
