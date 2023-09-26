package com.sismics.docs.rest;

import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.EMF;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.After;
import org.junit.Before;

public abstract class BaseTransactionalTest {

    @Before
    public void setUp() {
        // Initialize the entity manager
        EntityManager em = EMF.get().createEntityManager();
        ThreadLocalContext context = ThreadLocalContext.get();
        context.setEntityManager(em);
        em.getTransaction().begin();
    }

    @After
    public void tearDown() {
        ThreadLocalContext.get().getEntityManager().getTransaction().rollback();
    }

    protected User createUser(String userName) throws Exception {
        UserDao userDao = new UserDao();
        User user = new User();
        user.setUsername(userName);
        user.setPassword("12345678");
        user.setEmail("toto@docs.com");
        user.setRoleId("admin");
        user.setStorageQuota(100_000L);
        userDao.create(user, userName);
        return user;
    }


}
