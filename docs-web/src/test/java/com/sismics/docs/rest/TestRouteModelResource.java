package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;


/**
 * Test the route model resource.
 *
 * @author bgamard
 */
public class TestRouteModelResource extends BaseJerseyTest {
    /**
     * Test the route model resource.
     */
    @Test
    public void testRouteModelResource() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);

        // Login routeModel1
        clientUtil.createUser("routeModel1");
        String routeModel1Token = clientUtil.login("routeModel1");

        // Create a tag
        JsonObject json = target().path("/tag").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("name", "TagRoute")
                        .param("color", "#ff0000")), JsonObject.class);
        String tagRouteId = json.getString("id");

        // Get all route models with admin
        json = target().path("/routemodel")
                .queryParam("sort_column", "2")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        JsonArray routeModels = json.getJsonArray("routemodels");
        Assert.assertEquals(1, routeModels.size());

        // Create a route model without actions
        json = target().path("/routemodel").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("name", "Workflow validation 1")
                        .param("steps", "[{\"type\":\"VALIDATE\",\"transitions\":[{\"name\":\"VALIDATED\",\"actions\":[]}],\"target\":{\"name\":\"administrators\",\"type\":\"GROUP\"},\"name\":\"Check the document's metadata\"}]")), JsonObject.class);
        String routeModelId = json.getString("id");

        // Get all route models with admin
        json = target().path("/routemodel")
                .queryParam("sort_column", "2")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        routeModels = json.getJsonArray("routemodels");
        Assert.assertEquals(2, routeModels.size());
        Assert.assertEquals(routeModelId, routeModels.getJsonObject(0).getString("id"));
        Assert.assertEquals("Workflow validation 1", routeModels.getJsonObject(0).getString("name"));

        // Get all route models with routeModel1
        json = target().path("/routemodel")
                .queryParam("sort_column", "2")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, routeModel1Token)
                .get(JsonObject.class);
        routeModels = json.getJsonArray("routemodels");
        Assert.assertEquals(0, routeModels.size());

        // Add an ACL READ for routeModel1 with admin
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("source", routeModelId)
                        .param("perm", "READ")
                        .param("target", "routeModel1")
                        .param("type", "USER")), JsonObject.class);

        // Get all route models with routeModel1
        json = target().path("/routemodel")
                .queryParam("sort_column", "2")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, routeModel1Token)
                .get(JsonObject.class);
        routeModels = json.getJsonArray("routemodels");
        Assert.assertEquals(1, routeModels.size());

        // Get the route model
        json = target().path("/routemodel/" + routeModelId)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        Assert.assertEquals(routeModelId, json.getString("id"));
        Assert.assertEquals("Workflow validation 1", json.getString("name"));
        Assert.assertEquals("[{\"type\":\"VALIDATE\",\"transitions\":[{\"name\":\"VALIDATED\",\"actions\":[]}],\"target\":{\"name\":\"administrators\",\"type\":\"GROUP\"},\"name\":\"Check the document's metadata\"}]", json.getString("steps"));
        JsonArray acls = json.getJsonArray("acls");
        Assert.assertEquals(3, acls.size());// 2 for admin, 1 for routeModel1

        // Update the route model with actions
        target().path("/routemodel/" + routeModelId).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("name", "Workflow validation 2")
                        .param("steps", "[{\"type\":\"APPROVE\",\"transitions\":[{\"name\":\"APPROVED\",\"actions\":[{\"type\":\"ADD_TAG\",\"tag\":\"" + tagRouteId + "\"}]},{\"name\":\"REJECTED\",\"actions\":[]}],\"target\":{\"name\":\"administrators\",\"type\":\"GROUP\"},\"name\":\"Check the document's metadata\"}]")), JsonObject.class);

        // Get the route model
        json = target().path("/routemodel/" + routeModelId)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        Assert.assertEquals(routeModelId, json.getString("id"));
        Assert.assertEquals("Workflow validation 2", json.getString("name"));
        Assert.assertEquals("[{\"type\":\"APPROVE\",\"transitions\":[{\"name\":\"APPROVED\",\"actions\":[{\"type\":\"ADD_TAG\",\"tag\":\"" + tagRouteId + "\"}]},{\"name\":\"REJECTED\",\"actions\":[]}],\"target\":{\"name\":\"administrators\",\"type\":\"GROUP\"},\"name\":\"Check the document's metadata\"}]", json.getString("steps"));

        // Delete the route model
        target().path("/routemodel/" + routeModelId)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete(JsonObject.class);

        // Get all route models
        json = target().path("/routemodel")
                .queryParam("sort_column", "2")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        routeModels = json.getJsonArray("routemodels");
        Assert.assertEquals(1, routeModels.size());
    }
}