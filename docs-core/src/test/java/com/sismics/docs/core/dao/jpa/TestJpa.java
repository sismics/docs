package com.sismics.docs.core.dao.jpa;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.TransactionUtil;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Tests the persistance layer.
 * 
 * @author jtremeaux
 */
public class TestJpa extends BaseTransactionalTest {
    @Test
    public void testJpa() throws Exception {
        // Create a user
        UserDao userDao = new UserDao();
        User user = new User();
        user.setUsername("username");
        user.setEmail("toto@docs.com");
        user.setLocaleId("fr");
        user.setRoleId("admin");
        String id = userDao.create(user);
        
        TransactionUtil.commit();

        // Search a user by his ID
        user = userDao.getById(id);
        Assert.assertNotNull(user);
        Assert.assertEquals("toto@docs.com", user.getEmail());
    }
}
