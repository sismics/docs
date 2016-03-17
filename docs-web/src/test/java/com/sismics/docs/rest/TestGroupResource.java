package com.sismics.docs.rest;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.junit.Test;

import com.sismics.util.filter.TokenBasedSecurityFilter;


/**
 * Test the group resource.
 * 
 * @author bgamard
 */
public class TestGroupResource extends BaseJerseyTest {
    /**
     * Test the group resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testGroupResource() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);
        
        // Create a group
        target().path("/group").request()
            .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
            .put(Entity.form(new Form()
                    .param("name", "Group 1")), JsonObject.class);
    }
}