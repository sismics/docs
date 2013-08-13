package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.core.model.jpa.Locale;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

/**
 * Locale DAO.
 * 
 * @author jtremeaux
 */
public class LocaleDao {
    /**
     * Gets a locale by its ID.
     * 
     * @param id Locale ID
     * @return Locale
     */
    public Locale getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(Locale.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * Returns the list of all locales.
     * 
     * @return List of locales
     */
    @SuppressWarnings("unchecked")
    public List<Locale> findAll() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select l from Locale l order by l.id");
        return q.getResultList();
    }
}
