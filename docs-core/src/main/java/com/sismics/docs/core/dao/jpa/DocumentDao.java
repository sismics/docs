package com.sismics.docs.core.dao.jpa;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sismics.docs.core.dao.jpa.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
import com.sismics.docs.core.dao.lucene.LuceneDao;
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
        em.persist(document);
        
        return document.getId();
    }
    
    /**
     * Returns the list of all documents.
     * 
     * @return List of documents
     */
    @SuppressWarnings("unchecked")
    public List<Document> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select d from Document d where d.deleteDate is null");
        return q.getResultList();
    }
    
    /**
     * Returns an active document.
     * 
     * @param id Document ID
     * @return Document
     */
    public Document getDocument(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select d from Document d where d.id = :id and d.deleteDate is null");
        q.setParameter("id", id);
        return (Document) q.getSingleResult();
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
        q.setParameter("documentId", id);
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update Share s set s.deleteDate = :dateNow where s.documentId = :documentId and s.deleteDate is null");
        q.setParameter("documentId", id);
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
     * @throws Exception 
     */
    public void findByCriteria(PaginatedList<DocumentDto> paginatedList, DocumentCriteria criteria, SortCriteria sortCriteria) throws Exception {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        List<String> criteriaList = new ArrayList<String>();
        
        StringBuilder sb = new StringBuilder("select d.DOC_ID_C c0, d.DOC_TITLE_C c1, d.DOC_DESCRIPTION_C c2, d.DOC_CREATEDATE_D c3, d.DOC_LANGUAGE_C c4, s.SHA_ID_C is not null c5 ");
        sb.append(" from T_DOCUMENT d ");
        sb.append(" left join T_SHARE s on s.SHA_IDDOCUMENT_C = d.DOC_ID_C and s.SHA_DELETEDATE_D is null ");
        sb.append(" left join T_FILE f on f.FIL_IDDOC_C = d.DOC_ID_C and f.FIL_DELETEDATE_D is null ");
        
        // Adds search criteria
        if (criteria.getUserId() != null) {
            criteriaList.add("d.DOC_IDUSER_C = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        if (!Strings.isNullOrEmpty(criteria.getSearch())) {
            LuceneDao luceneDao = new LuceneDao();
            Set<String> documentIdList = luceneDao.search(criteria.getUserId(), criteria.getSearch());
            if (documentIdList.size() == 0) {
                // If the search doesn't find any document, the request should return nothing
                documentIdList.add(UUID.randomUUID().toString());
            }
            criteriaList.add("d.DOC_ID_C in :documentIdList");
            parameterMap.put("documentIdList", documentIdList);
        }
        if (criteria.getCreateDateMin() != null) {
            criteriaList.add("d.DOC_CREATEDATE_D >= :createDateMin");
            parameterMap.put("createDateMin", criteria.getCreateDateMin());
        }
        if (criteria.getCreateDateMax() != null) {
            criteriaList.add("d.DOC_CREATEDATE_D <= :createDateMax");
            parameterMap.put("createDateMax", criteria.getCreateDateMax());
        }
        if (criteria.getTagIdList() != null && !criteria.getTagIdList().isEmpty()) {
            int index = 0;
            for (String tagId : criteria.getTagIdList()) {
                sb.append(" left join T_DOCUMENT_TAG dt" + index + " on dt" + index + ".DOT_IDDOCUMENT_C = d.DOC_ID_C and dt" + index + ".DOT_IDTAG_C = :tagId" + index + " ");
                criteriaList.add("dt" + index + ".DOT_ID_C is not null");
                parameterMap.put("tagId" + index, tagId);
                index++;
            }
        }
        if (criteria.getShared() != null && criteria.getShared()) {
            criteriaList.add("s.SHA_ID_C is not null");
        }
        if (criteria.getLanguage() != null) {
            criteriaList.add("d.DOC_LANGUAGE_C = :language");
            parameterMap.put("language", criteria.getLanguage());
        }
        
        criteriaList.add("d.DOC_DELETEDATE_D is null");
        
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
            documentDto.setLanguage((String) o[i++]);
            documentDto.setShared((Boolean) o[i++]);
            documentDtoList.add(documentDto);
        }

        paginatedList.setResultList(documentDtoList);
    }
}
