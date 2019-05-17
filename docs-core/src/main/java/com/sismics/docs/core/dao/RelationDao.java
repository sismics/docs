package com.sismics.docs.core.dao;

import com.sismics.docs.core.dao.dto.RelationDto;
import com.sismics.docs.core.model.jpa.Relation;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * Relation DAO.
 * 
 * @author bgamard
 */
public class RelationDao {
    /**
     * Get all relations from/to a document.
     * 
     * @param documentId Document ID
     * @return List of relations
     */
    @SuppressWarnings("unchecked")
    public List<RelationDto> getByDocumentId(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select d.DOC_ID_C, d.DOC_TITLE_C, r.REL_IDDOCFROM_C ");
        sb.append(" from T_RELATION r ");
        sb.append(" join T_DOCUMENT d on d.DOC_ID_C = r.REL_IDDOCFROM_C and r.REL_IDDOCFROM_C != :documentId or d.DOC_ID_C = r.REL_IDDOCTO_C and r.REL_IDDOCTO_C != :documentId ");
        sb.append(" where (r.REL_IDDOCFROM_C = :documentId or r.REL_IDDOCTO_C = :documentId) ");
        sb.append(" and r.REL_DELETEDATE_D is null ");
        sb.append(" order by d.DOC_TITLE_C ");
        
        // Perform the query
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("documentId", documentId);
        List<Object[]> l = q.getResultList();
        
        // Assemble results
        List<RelationDto> relationDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            RelationDto relationDto = new RelationDto();
            relationDto.setId((String) o[i++]);
            relationDto.setTitle((String) o[i++]);
            String fromDocId = (String) o[i];
            relationDto.setSource(documentId.equals(fromDocId));
            relationDtoList.add(relationDto);
        }
        return relationDtoList;
    }
    
    /**
     * Update relations on a document.
     * 
     * @param documentId Document ID
     * @param documentIdSet Set of document ID
     */
    public void updateRelationList(String documentId, Set<String> documentIdSet) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get current relations from this document
        Query q = em.createQuery("select r from Relation r where r.fromDocumentId = :documentId and r.deleteDate is null");
        q.setParameter("documentId", documentId);
        @SuppressWarnings("unchecked")
        List<Relation> relationList = q.getResultList();
        
        // Deleting relations no longer there
        for (Relation relation : relationList) {
            if (!documentIdSet.contains(relation.getToDocumentId())) {
                relation.setDeleteDate(new Date());
            }
        }
        
        // Adding new relations
        for (String targetDocId : documentIdSet) {
            boolean found = false;
            for (Relation relation : relationList) {
                if (relation.getToDocumentId().equals(targetDocId)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                Relation relation = new Relation();
                relation.setId(UUID.randomUUID().toString());
                relation.setFromDocumentId(documentId);
                relation.setToDocumentId(targetDocId);
                em.persist(relation);
            }
        }
    }
}

