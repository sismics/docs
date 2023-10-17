package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.model.jpa.PasswordRecovery;
import com.sismics.util.context.ThreadLocalContext;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.Date;
import java.util.UUID;

/**
 * Password recovery DAO.
 * 
 * @author jtremeaux
 */
public class PasswordRecoveryDao {
    /**
     * Create a new password recovery request.
     * 
     * @param passwordRecovery Password recovery
     * @return Unique identifier
     */
    public String create(PasswordRecovery passwordRecovery) {
        passwordRecovery.setId(UUID.randomUUID().toString());
        passwordRecovery.setCreateDate(new Date());

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(passwordRecovery);
        
        return passwordRecovery.getId();
    }
    
    /**
     * Search an active password recovery by unique identifier.
     * 
     * @param id Unique identifier
     * @return Password recovery
     */
    public PasswordRecovery getActiveById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            Query q = em.createQuery("select r from PasswordRecovery r where r.id = :id and r.createDate > :createDateMin and r.deleteDate is null");
            q.setParameter("id", id);
            q.setParameter("createDateMin", new DateTime().withFieldAdded(DurationFieldType.hours(), -1 * Constants.PASSWORD_RECOVERY_EXPIRATION_HOUR).toDate());
            return (PasswordRecovery) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Deletes active password recovery by username.
     * 
     * @param username Username
     */
    public void deleteActiveByLogin(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("update PasswordRecovery r set r.deleteDate = :deleteDate where r.username = :username and r.createDate > :createDateMin and r.deleteDate is null");
        q.setParameter("username", username);
        q.setParameter("deleteDate", new Date());
        q.setParameter("createDateMin", new DateTime().withFieldAdded(DurationFieldType.hours(), -1 * Constants.PASSWORD_RECOVERY_EXPIRATION_HOUR).toDate());
        q.executeUpdate();
    }
}
