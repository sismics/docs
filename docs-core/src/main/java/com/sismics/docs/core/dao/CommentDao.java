package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.dto.CommentDto;
import com.sismics.docs.core.model.jpa.Comment;
import com.sismics.docs.core.util.AuditLogUtil;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Comment DAO.
 * 
 * @author bgamard
 */
public class CommentDao {
    /**
     * Creates a new comment.
     * 
     * @param comment Comment
     * @param userId User ID
     * @return New ID
     */
    public String create(Comment comment, String userId) {
        // Create the UUID
        comment.setId(UUID.randomUUID().toString());
        
        // Create the comment
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        comment.setCreateDate(new Date());
        em.persist(comment);
        
        // Create audit log
        AuditLogUtil.create(comment, AuditLogType.CREATE, userId);
        
        return comment.getId();
    }
    
    /**
     * Deletes a comment.
     * 
     * @param id Comment ID
     * @param userId User ID
     */
    public void delete(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the comment
        Query q = em.createQuery("select c from Comment c where c.id = :id and c.deleteDate is null");
        q.setParameter("id", id);
        Comment commentDb = (Comment) q.getSingleResult();
        
        // Delete the comment
        Date dateNow = new Date();
        commentDb.setDeleteDate(dateNow);

        // Create audit log
        AuditLogUtil.create(commentDb, AuditLogType.DELETE, userId);
    }
    
    /**
     * Gets an active comment by its ID.
     * 
     * @param id Comment ID
     * @return Comment
     */
    public Comment getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select c from Comment c where c.id = :id and c.deleteDate is null");
            q.setParameter("id", id);
            return (Comment) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Get all comments on a document.
     * 
     * @param documentId Document ID
     * @return List of comments
     */
    public List<CommentDto> getByDocumentId(String documentId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        StringBuilder sb = new StringBuilder("select c.COM_ID_C, c.COM_CONTENT_C, c.COM_CREATEDATE_D, u.USE_USERNAME_C, u.USE_EMAIL_C from T_COMMENT c, T_USER u");
        sb.append(" where c.COM_IDDOC_C = :documentId and c.COM_IDUSER_C = u.USE_ID_C and c.COM_DELETEDATE_D is null ");
        sb.append(" order by c.COM_CREATEDATE_D asc ");
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("documentId", documentId);
        @SuppressWarnings("unchecked")
        List<Object[]> l = q.getResultList();
        
        List<CommentDto> commentDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            CommentDto commentDto = new CommentDto();
            commentDto.setId((String) o[i++]);
            commentDto.setContent((String) o[i++]);
            commentDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            commentDto.setCreatorName((String) o[i++]);
            commentDto.setCreatorEmail((String) o[i]);
            commentDtoList.add(commentDto);
        }
        return commentDtoList;
    }
}
