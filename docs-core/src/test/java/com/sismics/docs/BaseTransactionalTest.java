package com.sismics.docs;

import com.sismics.BaseTest;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.EMF;
import org.junit.After;
import org.junit.Before;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Base class of tests with a transactional context.
 *
 * @author jtremeaux 
 */
public abstract class BaseTransactionalTest extends BaseTest {
    @Before
    public void setUp() throws Exception {
        // Initialize the entity manager
        EntityManager em = EMF.get().createEntityManager();
        ThreadLocalContext context = ThreadLocalContext.get();
        context.setEntityManager(em);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
    }

    @After
    public void tearDown() throws Exception {
    }
}
