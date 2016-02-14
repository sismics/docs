package com.sismics.docs.core.dao.jpa;

import java.util.UUID;

import javax.persistence.EntityManager;

import com.sismics.docs.core.model.jpa.Vocabulary;
import com.sismics.util.context.ThreadLocalContext;

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
     * @throws Exception
     */
    public String create(Vocabulary vocabulary) {
        // Create the UUID
        vocabulary.setId(UUID.randomUUID().toString());
        
        // Create the comment
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(vocabulary);
        
        return vocabulary.getId();
    }
}
