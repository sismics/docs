package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.Share;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Date;
import java.util.UUID;

/**
 * Share DAO.
 * 
 * @author bgamard
 */
public class ShareDao {
    /**
     * Creates a new share.
     * 
     * @param share Share
     * @return New ID
     */
    public String create(Share share) {
        // Create the UUID
        share.setId(UUID.randomUUID().toString());
        
        // Create the share
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        share.setCreateDate(new Date());
        em.persist(share);
        
        return share.getId();
    }
    
    /**
     * Deletes a share.
     * 
     * @param id Share ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the share
        Query q = em.createQuery("select s from Share s where s.id = :id and s.deleteDate is null");
        q.setParameter("id", id);
        Share shareDb = (Share) q.getSingleResult();
        
        // Delete the share
        Date dateNow = new Date();
        shareDb.setDeleteDate(dateNow);
        
        // Delete the linked ACL
        q = em.createQuery("update Acl a set a.deleteDate = :dateNow where a.targetId = :targetId");
        q.setParameter("targetId", id);
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
    }
}
