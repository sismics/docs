package com.sismics.docs.core.dao.jpa;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.sismics.docs.core.model.jpa.FileShare;
import com.sismics.util.context.ThreadLocalContext;

/**
 * File share DAO.
 * 
 * @author bgamard
 */
public class FileShareDao {
    /**
     * Creates a new file share.
     * 
     * @param fileShare File share
     * @return New ID
     * @throws Exception
     */
    public String create(FileShare fileShare) {
        // Create the UUID
        fileShare.setId(UUID.randomUUID().toString());
        
        // Create the file
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        fileShare.setCreateDate(new Date());
        em.persist(fileShare);
        
        return fileShare.getId();
    }
    
    /**
     * Returns an active file share.
     * 
     * @param id File ID
     * @return Document
     */
    public FileShare getFileShare(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select fs from FileShare fs where fs.id = :id and fs.deleteDate is null");
        q.setParameter("id", id);
        try {
            return (FileShare) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes a file share.
     * 
     * @param id File ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the document
        Query q = em.createQuery("select fs from FileShare fs where fs.id = :id and fs.deleteDate is null");
        q.setParameter("id", id);
        FileShare fileShareDb = (FileShare) q.getSingleResult();
        
        // Delete the document
        Date dateNow = new Date();
        fileShareDb.setDeleteDate(dateNow);
    }
    
    /**
     * Gets a file share by its ID.
     * 
     * @param id Document ID
     * @return Document
     */
    public FileShare getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(FileShare.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Get file shares by file ID.
     * 
     * @param fileId File ID
     * @return List of file shares
     */
    @SuppressWarnings("unchecked")
    public List<FileShare> getByFileId(String fileId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select fs from FileShare fs where fs.fileId = :fileId and fs.deleteDate is null");
        q.setParameter("fileId", fileId);
        return q.getResultList();
    }
}
