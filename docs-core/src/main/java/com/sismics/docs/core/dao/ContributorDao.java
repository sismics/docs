package com.sismics.docs.core.dao;

import com.sismics.docs.core.dao.dto.ContributorDto;
import com.sismics.docs.core.model.jpa.Contributor;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contributor DAO.
 * 
 * @author bgamard
 */
public class ContributorDao {
    /**
     * Creates a new contributor.
     * 
     * @param contributor Contributor
     * @return New ID
     */
    public String create(Contributor contributor) {
        // Create the UUID
        contributor.setId(UUID.randomUUID().toString());
        
        // Create the contributor
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(contributor);
        
        return contributor.getId();
    }
    
    /**
     * Returns the list of all contributors by document.
     * 
     * @param documentId Document ID
     * @return List of contributors
     */
    @SuppressWarnings("unchecked")
    public List<Contributor> findByDocumentId(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select c from Contributor c where c.documentId = :documentId");
        q.setParameter("documentId", documentId);
        return q.getResultList();
    }
    
    /**
     * Returns the list of all contributors by document.
     * 
     * @param documentId Document ID
     * @return List of contributors
     */
    @SuppressWarnings("unchecked")
    public List<ContributorDto> getByDocumentId(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select distinct u.USE_USERNAME_C, u.USE_EMAIL_C from T_CONTRIBUTOR c ");
        sb.append(" join T_USER u on u.USE_ID_C = c.CTR_IDUSER_C ");
        sb.append(" where c.CTR_IDDOC_C = :documentId ");
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("documentId", documentId);
        List<Object[]> l = q.getResultList();
        
        // Assemble results
        List<ContributorDto> contributorDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            ContributorDto contributorDto = new ContributorDto();
            contributorDto.setUsername((String) o[i++]);
            contributorDto.setEmail((String) o[i]);
            contributorDtoList.add(contributorDto);
        }
        return contributorDtoList;
    }
}
