package com.sismics.docs.core.util;

import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.EMF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Database transaction utils.
 *
 * @author jtremeaux 
 */
public class TransactionUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(TransactionUtil.class);

    /**
     * Encapsulate a process into a transactionnal context.
     * 
     * @param runnable
     */
    public static void handle(Runnable runnable) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        if (em != null) {
            // We are already in a transactional context, nothing to do
            runnable.run();
            return;
        }
        
        try {
            em = EMF.get().createEntityManager();
        } catch (Exception e) {
            log.error("Cannot create entity manager", e);
        }
        ThreadLocalContext context = ThreadLocalContext.get();
        context.setEntityManager(em);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        
        try {
            runnable.run();
        } catch (Exception e) {
            ThreadLocalContext.cleanup();
            
            log.error("An exception occured, rolling back current transaction", e);

            // If an unprocessed error comes up, rollback the transaction
            if (em.isOpen()) {
                if (em.getTransaction() != null && em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                
                try {
                    em.close();
                } catch (Exception ce) {
                    log.error("Error closing entity manager", ce);
                }
            }
            return;
        }
        
        ThreadLocalContext.cleanup();

        // No error in the current request : commit the transaction
        if (em.isOpen()) {
            if (em.getTransaction() != null && em.getTransaction().isActive()) {
                em.getTransaction().commit();
                
                try {
                    em.close();
                } catch (Exception e) {
                    log.error("Error closing entity manager", e);
                }
            }
        }
    }
    
    /**
     * Commits the current transaction, and flushes the changes to the database.
     */
    public static void commit() {
        EntityTransaction tx = ThreadLocalContext.get().getEntityManager().getTransaction();
        tx.commit();
        tx.begin();
    }
}
