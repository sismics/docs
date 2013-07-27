package com.sismics.docs.core.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.util.context.ThreadLocalContext;

/**
 * Configuration parameter DAO.
 * 
 * @author jtremeaux
 */
public class ConfigDao {
    /**
     * Gets a configuration parameter by its ID.
     * 
     * @param id Configuration parameter ID
     * @return Configuration parameter
     */
    public Config getById(ConfigType id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Prevents from getting parameters outside of a transactional context (e.g. jUnit)
        if (em == null) {
            return null;
        }
        
        try {
            return em.find(Config.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
}
