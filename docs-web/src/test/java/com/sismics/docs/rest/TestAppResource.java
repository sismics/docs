package com.sismics.docs.rest;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.EMF;
import org.junit.Assert;
import org.junit.Test;

import com.sismics.util.filter.TokenBasedSecurityFilter;


/**
 * Test the app resource.
 * 
 * @author jtremeaux
 */
public class TestAppResource extends BaseJerseyTest {
    /**
     * Test the API resource.
     */
    @Test
    public void testAppResource() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);
        
        // Check the application info
        JsonObject json = target().path("/app").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        String currentVersion = json.getString("current_version");
        Assert.assertNotNull(currentVersion);
        String minVersion = json.getString("min_version");
        Assert.assertNotNull(minVersion);
        Long freeMemory = json.getJsonNumber("free_memory").longValue();
        Assert.assertTrue(freeMemory > 0);
        Long totalMemory = json.getJsonNumber("total_memory").longValue();
        Assert.assertTrue(totalMemory > 0 && totalMemory > freeMemory);
        
        // Rebuild Lucene index
        Response response = target().path("/app/batch/reindex").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Clean storage
        response = target().path("/app/batch/clean_storage").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Recompute quota
        response = target().path("/app/batch/recompute_quota").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Create a tag
        json = target().path("/tag").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("name", "Tag4")
                        .param("color", "#00ff00")), JsonObject.class);
        String tagId = json.getString("id");

        // Init transactional context
        EntityManager em = EMF.get().createEntityManager();
        ThreadLocalContext context = ThreadLocalContext.get();
        context.setEntityManager(em);
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // Remove base ACLs
        AclDao aclDao = new AclDao();
        aclDao.delete(tagId, PermType.READ, "admin", "admin");
        aclDao.delete(tagId, PermType.WRITE, "admin", "admin");
        Assert.assertEquals(0, aclDao.getBySourceId(tagId).size());
        tx.commit();

        // Add base ACLs to tags
        response = target().path("/app/batch/tag_acls").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(2, aclDao.getBySourceId(tagId).size());
    }

    /**
     * Test the log resource.
     */
    @Test
    public void testLogResource() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);
        
        // Check the logs (page 1)
        JsonObject json = target().path("/app/log")
                .queryParam("level", "DEBUG")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        JsonArray logs = json.getJsonArray("logs");
        Assert.assertTrue(logs.size() > 0);
        Long date1 = logs.getJsonObject(0).getJsonNumber("date").longValue();
        Long date2 = logs.getJsonObject(9).getJsonNumber("date").longValue();
        Assert.assertTrue(date1 >= date2);
        
        // Check the logs (page 2)
        json = target().path("/app/log")
                .queryParam("offset",  "10")
                .queryParam("level", "DEBUG")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        logs = json.getJsonArray("logs");
        Assert.assertTrue(logs.size() > 0);
        Long date3 = logs.getJsonObject(0).getJsonNumber("date").longValue();
        Long date4 = logs.getJsonObject(9).getJsonNumber("date").longValue();
        Assert.assertTrue(date3 >= date4);
    }
}