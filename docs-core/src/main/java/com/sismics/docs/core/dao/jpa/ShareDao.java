package com.sismics.docs.core.dao.jpa;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.Share;
import com.sismics.util.context.ThreadLocalContext;

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
     * @throws Exception
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
     * Returns an active share.
     * 
     * @param id Share ID
     * @return Document
     */
    public Share getShare(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select s from Share s where s.id = :id and s.deleteDate is null");
        q.setParameter("id", id);
        try {
            return (Share) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
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
    }
    
    /**
     * Get shares by document ID.
     * 
     * @param documentId Document ID
     * @return List of shares
     */
    @SuppressWarnings("unchecked")
    public List<Share> getByDocumentId(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select s from Share s where s.documentId = :documentId and s.deleteDate is null");
        q.setParameter("documentId", documentId);
        return q.getResultList();
    }
    
    /**
     * Check if a document is visible. 
     * 
     * @param document Document to check for visibility
     * @param userId Optional user trying to access the document
     * @param shareId Optional share to access the document
     * @return True if the document is visible
     */
    public boolean checkVisibility(Document document, String userId, String shareId) {
        // The user owns the document
        if (document.getUserId().equals(userId)) {
            return true;
        }
        
        // The share is linked to the document
        if (shareId != null) {
            Share share = getShare(shareId);
            if (share.getDocumentId().equals(document.getId())) {
                return true;
            }
        }
        
        return false;
    }
}
