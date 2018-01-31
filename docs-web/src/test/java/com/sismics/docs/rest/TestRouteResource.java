package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import java.util.Date;


/**
 * Test the route resource.
 * 
 * @author bgamard
 */
public class TestRouteResource extends BaseJerseyTest {
    /**
     * Test the route resource.
     */
    @Test
    public void testRouteResource() {
        // Login route1
        clientUtil.createUser("route1");
        String route1Token = clientUtil.login("route1");

        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);
        
        // Get all route models
        JsonObject json = target().path("/routemodel")
                .queryParam("sort_column", "2")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        JsonArray routeModels = json.getJsonArray("routemodels");
        Assert.assertEquals(1, routeModels.size());

        // Create a document
        long create1Date = new Date().getTime();
        json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .put(Entity.form(new Form()
                        .param("title", "My super title document 1")
                        .param("description", "My super description for document 1")
                        .param("language", "eng")
                        .param("create_date", Long.toString(create1Date))), JsonObject.class);
        String document1Id = json.getString("id");

        // Start the default route on document 1
        target().path("/route/start").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("routeModelId", routeModels.getJsonObject(0).getString("id"))), JsonObject.class);

        // Add an ACL READ for admin with route1
        // TODO Remove me when ACLs are automatically added on route step targets
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "READ")
                        .param("target", "admin")
                        .param("type", "USER")), JsonObject.class);

        // Get document 1 as route1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        JsonObject routeStep = json.getJsonObject("route_step");
        Assert.assertNotNull(routeStep);
        Assert.assertFalse(routeStep.getBoolean("transitionable"));

        // Get document 1 as admin
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        routeStep = json.getJsonObject("route_step");
        Assert.assertNotNull(routeStep);
        Assert.assertTrue(routeStep.getBoolean("transitionable"));
    }
}