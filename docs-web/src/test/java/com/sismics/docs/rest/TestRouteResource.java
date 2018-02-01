package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
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

        // Get the route on document 1
        json = target().path("/route")
                .queryParam("documentId", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        JsonArray routes = json.getJsonArray("routes");
        Assert.assertEquals(1, routes.size());
        JsonObject route = routes.getJsonObject(0);
        Assert.assertEquals("Document review", route.getString("name"));
        Assert.assertNotNull(route.getJsonNumber("create_date"));
        JsonArray steps = route.getJsonArray("steps");
        Assert.assertEquals(3, steps.size());
        JsonObject step = steps.getJsonObject(0);
        Assert.assertEquals("Check the document's metadata", step.getString("name"));
        Assert.assertEquals("VALIDATE", step.getString("type"));
        Assert.assertTrue(step.isNull("comment"));
        Assert.assertTrue(step.isNull("end_date"));
        Assert.assertTrue(step.isNull("validator_username"));
        Assert.assertTrue(step.isNull("transition"));
        JsonObject target = step.getJsonObject("target");
        Assert.assertEquals("administrators", target.getString("id"));
        Assert.assertEquals("administrators", target.getString("name"));
        Assert.assertEquals("GROUP", target.getString("type"));

        // Get document 1 as route1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        JsonObject routeStep = json.getJsonObject("route_step");
        Assert.assertNotNull(routeStep);
        Assert.assertFalse(routeStep.getBoolean("transitionable"));
        Assert.assertEquals("Check the document's metadata", routeStep.getString("name"));

        // Get document 1 as admin
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        routeStep = json.getJsonObject("route_step");
        Assert.assertNotNull(routeStep);
        Assert.assertTrue(routeStep.getBoolean("transitionable"));
        Assert.assertEquals("Check the document's metadata", routeStep.getString("name"));

        // Validate the current step with admin
        target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("transition", "VALIDATED")), JsonObject.class);

        // Get the route on document 1
        json = target().path("/route")
                .queryParam("documentId", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        routes = json.getJsonArray("routes");
        Assert.assertEquals(1, routes.size());
        route = routes.getJsonObject(0);
        Assert.assertNotNull(route.getJsonNumber("create_date"));
        steps = route.getJsonArray("steps");
        Assert.assertEquals(3, steps.size());
        step = steps.getJsonObject(0);
        Assert.assertEquals("VALIDATE", step.getString("type"));
        Assert.assertTrue(step.isNull("comment"));
        Assert.assertFalse(step.isNull("end_date"));
        Assert.assertEquals("admin", step.getString("validator_username"));
        Assert.assertEquals("VALIDATED", step.getString("transition"));

        // Get document 1 as admin
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        routeStep = json.getJsonObject("route_step");
        Assert.assertNotNull(routeStep);
        Assert.assertEquals("Add relevant files to the document", routeStep.getString("name"));

        // Validate the current step with admin
        target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("transition", "VALIDATED")
                        .param("comment", "OK")), JsonObject.class);

        // Get the route on document 1
        json = target().path("/route")
                .queryParam("documentId", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        routes = json.getJsonArray("routes");
        Assert.assertEquals(1, routes.size());
        route = routes.getJsonObject(0);
        Assert.assertNotNull(route.getJsonNumber("create_date"));
        steps = route.getJsonArray("steps");
        Assert.assertEquals(3, steps.size());
        step = steps.getJsonObject(1);
        Assert.assertEquals("VALIDATE", step.getString("type"));
        Assert.assertEquals("OK", step.getString("comment"));
        Assert.assertFalse(step.isNull("end_date"));
        Assert.assertEquals("admin", step.getString("validator_username"));
        Assert.assertEquals("VALIDATED", step.getString("transition"));

        // Get document 1 as admin
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        routeStep = json.getJsonObject("route_step");
        Assert.assertNotNull(routeStep);
        Assert.assertEquals("Approve the document", routeStep.getString("name"));

        // Validate the current step with admin
        target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("transition", "APPROVED")), JsonObject.class);

        // Get the route on document 1
        json = target().path("/route")
                .queryParam("documentId", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        routes = json.getJsonArray("routes");
        Assert.assertEquals(1, routes.size());
        route = routes.getJsonObject(0);
        Assert.assertNotNull(route.getJsonNumber("create_date"));
        steps = route.getJsonArray("steps");
        Assert.assertEquals(3, steps.size());
        step = steps.getJsonObject(2);
        Assert.assertEquals("APPROVE", step.getString("type"));
        Assert.assertTrue(step.isNull("comment"));
        Assert.assertFalse(step.isNull("end_date"));
        Assert.assertEquals("admin", step.getString("validator_username"));
        Assert.assertEquals("APPROVED", step.getString("transition"));

        // Get document 1 as admin
        Response response = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get();
        Assert.assertEquals(Response.Status.NOT_FOUND, Response.Status.fromStatusCode(response.getStatus()));

        // Get document 1 as route1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        Assert.assertFalse(json.containsKey("route_step"));
    }
}