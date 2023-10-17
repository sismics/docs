package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

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

    /**
     * Updates a configuration parameter.
     *
     * @param id Configuration parameter ID
     * @param value Configuration parameter value
     */
    public void update(ConfigType id, String value) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Config config = getById(id);
        if (config == null) {
            config = new Config();
            config.setId(id);
            config.setValue(value);
            em.persist(config);
        } else {
            config.setValue(value);
        }
    }
}
