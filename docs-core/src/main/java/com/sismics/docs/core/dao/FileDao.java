package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * File DAO.
 * 
 * @author bgamard
 */
public class FileDao {
    /**
     * Creates a new file.
     * 
     * @param file File
     * @param userId User ID
     * @return New ID
     */
    public String create(File file, String userId) {
        // Create the UUID
        file.setId(UUID.randomUUID().toString());
        
        // Create the file
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        file.setCreateDate(new Date());
        em.persist(file);
        
        // Create audit log
        AuditLogUtil.create(file, AuditLogType.CREATE, userId);
        
        return file.getId();
    }
    
    /**
     * Returns the list of all files.
     *
     * @param offset Offset
     * @param limit Limit
     * @return List of files
     */
    public List<File> findAll(int offset, int limit) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<File> q = em.createQuery("select f from File f where f.deleteDate is null", File.class);
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        return q.getResultList();
    }
    
    /**
     * Returns the list of all files from a user.
     * 
     * @param userId User ID
     * @return List of files
     */
    public List<File> findByUserId(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<File> q = em.createQuery("select f from File f where f.userId = :userId and f.deleteDate is null", File.class);
        q.setParameter("userId", userId);
        return q.getResultList();
    }

    /**
     * Returns a list of active files.
     *
     * @param ids Files IDs
     * @return List of files
     */
    public List<File> getFiles(List<String> ids) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<File> q = em.createQuery("select f from File f where f.id in :ids and f.deleteDate is null", File.class);
        q.setParameter("ids", ids);
        return q.getResultList();
    }
    
    /**
     * Returns an active file or null.
     * 
     * @param id File ID
     * @return File
     */
    public File getFile(String id) {
        List<File> files = getFiles(List.of(id));
        if (files.isEmpty()) {
            return null;
        } else {
            return files.get(0);
        }
    }
    
    /**
     * Returns an active file.
     * 
     * @param id File ID
     * @param userId User ID
     * @return File
     */
    public File getFile(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<File> q = em.createQuery("select f from File f where f.id = :id and f.userId = :userId and f.deleteDate is null", File.class);
        q.setParameter("id", id);
        q.setParameter("userId", userId);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes a file.
     * 
     * @param id File ID
     * @param userId User ID
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the file
        TypedQuery<File> q = em.createQuery("select f from File f where f.id = :id and f.deleteDate is null", File.class);
        q.setParameter("id", id);
        File fileDb = q.getSingleResult();
        
        // Delete the file
        Date dateNow = new Date();
        fileDb.setDeleteDate(dateNow);
        
        // Create audit log
        AuditLogUtil.create(fileDb, AuditLogType.DELETE, userId);
    }
    
    /**
     * Update a file.
     * 
     * @param file File to update
     * @return Updated file
     */
    public File update(File file) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the file
        TypedQuery<File> q = em.createQuery("select f from File f where f.id = :id and f.deleteDate is null", File.class);
        q.setParameter("id", file.getId());
        File fileDb = q.getSingleResult();

        // Update the file
        fileDb.setDocumentId(file.getDocumentId());
        fileDb.setName(file.getName());
        fileDb.setContent(file.getContent());
        fileDb.setOrder(file.getOrder());
        fileDb.setMimeType(file.getMimeType());
        fileDb.setVersionId(file.getVersionId());
        fileDb.setLatestVersion(file.isLatestVersion());
        fileDb.setSize(file.getSize());

        return file;
    }

    /**
     * Gets a file by its ID.
     * 
     * @param id File ID
     * @return File
     */
    public File getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<File> q = em.createQuery("select f from File f where f.id = :id and f.deleteDate is null", File.class);
        q.setParameter("id", id);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Get files by document ID or all orphan files of a user.
     * 
     * @param userId User ID
     * @param documentId Document ID
     * @return List of files
     */
    public List<File> getByDocumentId(String userId, String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        if (documentId == null) {
            TypedQuery<File> q = em.createQuery("select f from File f where f.documentId is null and f.deleteDate is null and f.latestVersion = true and f.userId = :userId order by f.createDate asc", File.class);
            q.setParameter("userId", userId);
            return q.getResultList();
        } else {
            return getByDocumentsIds(Collections.singleton(documentId));
        }
    }

    /**
     * Get files by documents IDs.
     *
     * @param documentIds Documents IDs
     * @return List of files
     */
    public List<File> getByDocumentsIds(Iterable<String> documentIds) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<File> q = em.createQuery("select f from File f where f.documentId in :documentIds and f.latestVersion = true and f.deleteDate is null order by f.order asc", File.class);
        q.setParameter("documentIds", documentIds);
        return q.getResultList();
    }

    /**
     * Get files count by documents IDs.
     *
     * @param documentIds Documents IDs
     * @return the number of files per document id
     */
    public Map<String, Long> countByDocumentsIds(Iterable<String> documentIds) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select f.documentId, count(*) from File f where f.documentId in :documentIds and f.latestVersion = true and f.deleteDate is null group by (f.documentId)");
        q.setParameter("documentIds", documentIds);
        Map<String, Long> result = new HashMap<>();
        q.getResultList().forEach(o -> {
            Object[] resultLine = (Object[]) o;
            result.put((String) resultLine[0], (Long) resultLine[1]);
        });
        return result;
    }

    /**
     * Get all files from a version.
     *
     * @param versionId Version ID
     * @return List of files
     */
    public List<File> getByVersionId(String versionId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<File> q = em.createQuery("select f from File f where f.versionId = :versionId and f.deleteDate is null order by f.order asc", File.class);
        q.setParameter("versionId", versionId);
        return q.getResultList();
    }

    public List<File> getFilesWithUnknownSize(int limit) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<File> q = em.createQuery("select f from File f where f.size = :size and f.deleteDate is null order by f.order asc", File.class);
        q.setParameter("size", File.UNKNOWN_SIZE);
        q.setMaxResults(limit);
        return q.getResultList();
    }
}
