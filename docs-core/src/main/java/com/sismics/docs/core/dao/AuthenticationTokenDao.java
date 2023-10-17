package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.AuthenticationToken;
import com.sismics.util.context.ThreadLocalContext;
import org.joda.time.DateTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Authentication token DAO.
 * 
 * @author jtremeaux
 */
public class AuthenticationTokenDao {
    /**
     * Gets an authentication token.
     * 
     * @param id Authentication token ID
     * @return Authentication token
     */
    public AuthenticationToken get(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(AuthenticationToken.class, id);
    }

    /**
     * Creates a new authentication token.
     * 
     * @param authenticationToken Authentication token
     * @return Authentication token ID
     */
    public String create(AuthenticationToken authenticationToken) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        authenticationToken.setId(UUID.randomUUID().toString());
        authenticationToken.setCreationDate(new Date());
        em.persist(authenticationToken);
        
        return authenticationToken.getId();
    }

    /**
     * Deletes the authentication token.
     * 
     * @param authenticationTokenId Authentication token ID
     * @throws Exception
     */
    public void delete(String authenticationTokenId) throws Exception {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        AuthenticationToken authenticationToken = em.find(AuthenticationToken.class, authenticationTokenId);
        if (authenticationToken != null) {
            em.remove(authenticationToken);
        } else {
            throw new Exception("Token not found: " + authenticationTokenId);
        }
    }

    /**
     * Deletes old short lived tokens.
     *
     * @param userId User ID
     * @throws Exception
     */
    public void deleteOldSessionToken(String userId) {
        StringBuilder sb = new StringBuilder("delete from T_AUTHENTICATION_TOKEN AS ato ");
        sb.append(" where ato.AUT_IDUSER_C = :userId and ato.AUT_LONGLASTED_B = :longLasted");
        sb.append(" and ato.AUT_LASTCONNECTIONDATE_D < :minDate ");

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("userId", userId);
        q.setParameter("longLasted", false);
        q.setParameter("minDate", DateTime.now().minusDays(1).toDate());
        q.executeUpdate();
    }

    /**
     * Deletes old short lived tokens.
     *
     * @param id Token id
     * @throws Exception
     */
    public void updateLastConnectionDate(String id) {
        StringBuilder sb = new StringBuilder("update T_AUTHENTICATION_TOKEN ato ");
        sb.append(" set AUT_LASTCONNECTIONDATE_D = :currentDate ");
        sb.append(" where ato.AUT_ID_C = :id");

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createNativeQuery(sb.toString());
        q.setParameter("currentDate", new Date());
        q.setParameter("id", id);
        q.executeUpdate();
    }
    
    /**
     * Returns all authentication tokens of an user.
     * 
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<AuthenticationToken> getByUserId(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select a from AuthenticationToken a where a.userId = :userId");
        q.setParameter("userId", userId);
        return q.getResultList();
    }
    
    /**
     * Deletes all authentication tokens of an user.
     * 
     * @param userId
     */
    public void deleteByUserId(String userId, String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("delete AuthenticationToken a where a.userId = :userId and a.id != :id");
        q.setParameter("userId", userId);
        q.setParameter("id", id);
        q.executeUpdate();
    }
}
