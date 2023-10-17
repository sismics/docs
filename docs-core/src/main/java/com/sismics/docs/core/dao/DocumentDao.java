package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
     * @param userId User ID
     * @return New ID
     */
    public String create(Document document, String userId) {
        // Create the UUID
        document.setId(UUID.randomUUID().toString());
        document.setUpdateDate(new Date());
        
        // Create the document
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(document);
        
        // Create audit log
        AuditLogUtil.create(document, AuditLogType.CREATE, userId);
        
        return document.getId();
    }
    
    /**
     * Returns the list of all active documents.
     *
     * @param offset Offset
     * @param limit Limit
     * @return List of documents
     */
    public List<Document> findAll(int offset, int limit) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<Document> q = em.createQuery("select d from Document d where d.deleteDate is null", Document.class);
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        return q.getResultList();
    }

    /**
     * Returns the list of all active documents from a user.
     * 
     * @param userId User ID
     * @return List of documents
     */
    public List<Document> findByUserId(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<Document> q = em.createQuery("select d from Document d where d.userId = :userId and d.deleteDate is null", Document.class);
        q.setParameter("userId", userId);
        return q.getResultList();
    }
    
    /**
     * Returns an active document with permission checking.
     * 
     * @param id Document ID
     * @param perm Permission needed
     * @param targetIdList List of targets
     * @return Document
     */
    public DocumentDto getDocument(String id, PermType perm, List<String> targetIdList) {
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(id, perm, targetIdList)) {
            return null;
        }

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select distinct d.DOC_ID_C, d.DOC_TITLE_C, d.DOC_DESCRIPTION_C, d.DOC_SUBJECT_C, d.DOC_IDENTIFIER_C, d.DOC_PUBLISHER_C, d.DOC_FORMAT_C, d.DOC_SOURCE_C, d.DOC_TYPE_C, d.DOC_COVERAGE_C, d.DOC_RIGHTS_C, d.DOC_CREATEDATE_D, d.DOC_UPDATEDATE_D, d.DOC_LANGUAGE_C, ");
        sb.append(" (select count(s.SHA_ID_C) from T_SHARE s, T_ACL ac where ac.ACL_SOURCEID_C = d.DOC_ID_C and ac.ACL_TARGETID_C = s.SHA_ID_C and ac.ACL_DELETEDATE_D is null and s.SHA_DELETEDATE_D is null) shareCount, ");
        sb.append(" (select count(f.FIL_ID_C) from T_FILE f where f.FIL_DELETEDATE_D is null and f.FIL_IDDOC_C = d.DOC_ID_C) fileCount, ");
        sb.append(" u.USE_USERNAME_C ");
        sb.append(" from T_DOCUMENT d ");
        sb.append(" join T_USER u on d.DOC_IDUSER_C = u.USE_ID_C ");
        sb.append(" where d.DOC_ID_C = :id and d.DOC_DELETEDATE_D is null ");

        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("id", id);

        Object[] o;
        try {
            o = (Object[]) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        
        DocumentDto documentDto = new DocumentDto();
        int i = 0;
        documentDto.setId((String) o[i++]);
        documentDto.setTitle((String) o[i++]);
        documentDto.setDescription((String) o[i++]);
        documentDto.setSubject((String) o[i++]);
        documentDto.setIdentifier((String) o[i++]);
        documentDto.setPublisher((String) o[i++]);
        documentDto.setFormat((String) o[i++]);
        documentDto.setSource((String) o[i++]);
        documentDto.setType((String) o[i++]);
        documentDto.setCoverage((String) o[i++]);
        documentDto.setRights((String) o[i++]);
        documentDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
        documentDto.setUpdateTimestamp(((Timestamp) o[i++]).getTime());
        documentDto.setLanguage((String) o[i++]);
        documentDto.setShared(((Number) o[i++]).intValue() > 0);
        documentDto.setFileCount(((Number) o[i++]).intValue());
        documentDto.setCreator((String) o[i]);
        return documentDto;
    }
    
    /**
     * Deletes a document.
     * 
     * @param id Document ID
     * @param userId User ID
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the document
        TypedQuery<Document> dq = em.createQuery("select d from Document d where d.id = :id and d.deleteDate is null", Document.class);
        dq.setParameter("id", id);
        Document documentDb = dq.getSingleResult();
        
        // Delete the document
        Date dateNow = new Date();
        documentDb.setDeleteDate(dateNow);

        // Delete linked data
        Query q = em.createQuery("update File f set f.deleteDate = :dateNow where f.documentId = :documentId and f.deleteDate is null");
        q.setParameter("documentId", id);
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update Acl a set a.deleteDate = :dateNow where a.sourceId = :documentId and a.deleteDate is null");
        q.setParameter("documentId", id);
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update DocumentTag dt set dt.deleteDate = :dateNow where dt.documentId = :documentId and dt.deleteDate is not null");
        q.setParameter("documentId", id);
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        q = em.createQuery("update Relation r set r.deleteDate = :dateNow where (r.fromDocumentId = :documentId or r.toDocumentId = :documentId) and r.deleteDate is not null");
        q.setParameter("documentId", id);
        q.setParameter("dateNow", dateNow);
        q.executeUpdate();
        
        // Create audit log
        AuditLogUtil.create(documentDb, AuditLogType.DELETE, userId);
    }
    
    /**
     * Gets an active document by its ID.
     * 
     * @param id Document ID
     * @return Document
     */
    public Document getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<Document> q = em.createQuery("select d from Document d where d.id = :id and d.deleteDate is null", Document.class);
        q.setParameter("id", id);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Update a document and log the action.
     * 
     * @param document Document to update
     * @param userId User ID
     * @return Updated document
     */
    public Document update(Document document, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Get the document
        TypedQuery<Document> q = em.createQuery("select d from Document d where d.id = :id and d.deleteDate is null", Document.class);
        q.setParameter("id", document.getId());
        Document documentDb = q.getSingleResult();

        // Update the document
        documentDb.setTitle(document.getTitle());
        documentDb.setDescription(document.getDescription());
        documentDb.setSubject(document.getSubject());
        documentDb.setIdentifier(document.getIdentifier());
        documentDb.setPublisher(document.getPublisher());
        documentDb.setFormat(document.getFormat());
        documentDb.setSource(document.getSource());
        documentDb.setType(document.getType());
        documentDb.setCoverage(document.getCoverage());
        documentDb.setRights(document.getRights());
        documentDb.setCreateDate(document.getCreateDate());
        documentDb.setLanguage(document.getLanguage());
        documentDb.setFileId(document.getFileId());
        documentDb.setUpdateDate(new Date());
        
        // Create audit log
        AuditLogUtil.create(documentDb, AuditLogType.UPDATE, userId);
        
        return documentDb;
    }

    /**
     * Update the file ID on a document.
     *
     * @param document Document
     */
    public void updateFileId(Document document) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery("update T_DOCUMENT d set DOC_IDFILE_C = :fileId, DOC_UPDATEDATE_D = :updateDate where d.DOC_ID_C = :id");
        query.setParameter("updateDate", new Date());
        query.setParameter("fileId", document.getFileId());
        query.setParameter("id", document.getId());
        query.executeUpdate();
    }

    /**
     * Returns the number of documents.
     *
     * @return Number of documents
     */
    public long getDocumentCount() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery("select count(d.DOC_ID_C) from T_DOCUMENT d where d.DOC_DELETEDATE_D is null");
        return ((Number) query.getSingleResult()).longValue();
    }
}
