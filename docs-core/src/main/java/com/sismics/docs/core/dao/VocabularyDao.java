package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.Vocabulary;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.List;
import java.util.UUID;

/**
 * Vocabulary DAO.
 * 
 * @author bgamard
 */
public class VocabularyDao {
    /**
     * Creates a new vocabulary entry.
     * 
     * @param vocabulary Vocabulary
     * @return New ID
     */
    public String create(Vocabulary vocabulary) {
        // Create the UUID
        vocabulary.setId(UUID.randomUUID().toString());
        
        // Create the comment
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(vocabulary);
        
        return vocabulary.getId();
    }

    /**
     * Get all vocabulary entries sharing a single name.
     * 
     * @param name Name
     * @return Vocabulary entries
     */
    @SuppressWarnings("unchecked")
    public List<Vocabulary> getByName(String name) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the entries
        Query q = em.createQuery("select v from Vocabulary v where v.name = :name order by v.order");
        q.setParameter("name", name);
        return q.getResultList();
    }

    /**
     * Get a vocabulary entry by ID.
     * 
     * @param id ID
     * @return Vocabulary
     */
    public Vocabulary getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return em.find(Vocabulary.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Update a vocabulary entry.
     * 
     * @param vocabulary Vocabulary to update
     */
    public Vocabulary update(Vocabulary vocabulary) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Get the vocabulary entry
        Query q = em.createQuery("select v from Vocabulary v where v.id = :id");
        q.setParameter("id", vocabulary.getId());
        Vocabulary vocabularyDb = (Vocabulary) q.getSingleResult();
        
        // Update the vocabulary entry
        vocabularyDb.setName(vocabulary.getName());
        vocabularyDb.setValue(vocabulary.getValue());
        vocabularyDb.setOrder(vocabulary.getOrder());
        
        return vocabularyDb;
    }
    
    /**
     * Deletes a vocabulary entry.
     * 
     * @param id Vocabulary ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
            
        // Get the vocabulary
        Query q = em.createQuery("select v from Vocabulary v where v.id = :id");
        q.setParameter("id", id);
        Vocabulary vocabularyDb = (Vocabulary) q.getSingleResult();
        
        em.remove(vocabularyDb);
    }
}
