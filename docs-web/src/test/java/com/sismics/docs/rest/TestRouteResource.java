package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;


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
    public void testRouteResource() throws Exception {
        // Login route1
        clientUtil.createUser("route1");
        String route1Token = clientUtil.login("route1");

        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);

        // Change SMTP configuration to target Wiser
        target().path("/app/config_smtp").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("hostname", "localhost")
                        .param("port", "2500")
                        .param("from", "contact@sismicsdocs.com")
                ), JsonObject.class);

        // Add an ACL READ for route1 with admin on the default workflow
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("source", "default-document-review")
                        .param("perm", "READ")
                        .param("target", "route1")
                        .param("type", "USER")), JsonObject.class);

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
        json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .put(Entity.form(new Form()
                        .param("title", "My super title document 1")
                        .param("language", "eng")), JsonObject.class);
        String document1Id = json.getString("id");

        // Start the default route on document 1
        json = target().path("/route/start").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("routeModelId", routeModels.getJsonObject(0).getString("id"))), JsonObject.class);
        JsonObject step = json.getJsonObject("route_step");
        Assert.assertEquals("Check the document's metadata", step.getString("name"));
        Assert.assertTrue(popEmail().contains("workflow step"));

        // List all documents with route1
        json = target().path("/document/list")
                .queryParam("sort_column", 3)
                .queryParam("asc", true)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        JsonArray documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());
        Assert.assertFalse(documents.getJsonObject(0).getBoolean("active_route"));

        // List all documents with admin
        json = target().path("/document/list")
                .queryParam("sort_column", 3)
                .queryParam("asc", true)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());
        Assert.assertTrue(documents.getJsonObject(0).getBoolean("active_route"));
        Assert.assertEquals("Check the document's metadata", documents.getJsonObject(0).getString("current_step_name"));

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
        step = steps.getJsonObject(0);
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
        json = target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("transition", "VALIDATED")), JsonObject.class);
        step = json.getJsonObject("route_step");
        Assert.assertEquals("Add relevant files to the document", step.getString("name"));
        Assert.assertTrue(json.getBoolean("readable"));
        Assert.assertTrue(popEmail().contains("workflow step"));

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
        json = target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("transition", "VALIDATED")
                        .param("comment", "OK")), JsonObject.class);
        step = json.getJsonObject("route_step");
        Assert.assertEquals("Approve the document", step.getString("name"));
        Assert.assertTrue(json.getBoolean("readable"));
        Assert.assertTrue(popEmail().contains("workflow step"));

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
        json = target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("transition", "APPROVED")), JsonObject.class);
        Assert.assertFalse(json.containsKey("route_step"));
        Assert.assertTrue(json.getBoolean("readable")); // Admin can read everything
        Assert.assertNull(popEmail()); // Last step does not send any email

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
        target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);

        // Get document 1 as route1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        Assert.assertFalse(json.containsKey("route_step"));

        // List all documents with route1
        json = target().path("/document/list")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());
        Assert.assertFalse(documents.getJsonObject(0).getBoolean("active_route"));

        // List all documents with admin
        json = target().path("/document/list")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size()); // Admin can read all documents

        // Start the default route on document 1
        target().path("/route/start").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("routeModelId", routeModels.getJsonObject(0).getString("id"))), JsonObject.class);
        Assert.assertTrue(popEmail().contains("workflow step"));

        // Get document 1 as route1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        Assert.assertTrue(json.containsKey("route_step"));

        // Get document 1 as admin
        Response response = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get();
        Assert.assertEquals(Response.Status.OK, Response.Status.fromStatusCode(response.getStatus()));

        // List all documents with route1
        json = target().path("/document/list")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());
        Assert.assertFalse(documents.getJsonObject(0).getBoolean("active_route"));

        // List all documents with admin
        json = target().path("/document/list")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());
        Assert.assertTrue(documents.getJsonObject(0).getBoolean("active_route"));

        // Search documents with admin
        json = target().path("/document/list")
                .queryParam("search", "workflow:me")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());

        // Cancel the route on document 1
        target().path("/route")
                .queryParam("documentId", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .delete(JsonObject.class);

        // Get document 1 as route1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        Assert.assertFalse(json.containsKey("route_step"));

        // Get document 1 as admin
        target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class); // Admin can see all documents

        // List all documents with route1
        json = target().path("/document/list")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, route1Token)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());
        Assert.assertFalse(documents.getJsonObject(0).getBoolean("active_route"));

        // List all documents with admin
        json = target().path("/document/list")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());
    }

    /**
     * Test tag actions on workflow step.
     */
    @Test
    public void testTagActions() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);

        // Create an Approved tag
        JsonObject json = target().path("/tag").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("name", "Approved")
                        .param("color", "#ff0000")), JsonObject.class);
        String tagApprovedId = json.getString("id");

        // Create a Pending tag
        json = target().path("/tag").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("name", "Pending")
                        .param("color", "#ff0000")), JsonObject.class);
        String tagPendingId = json.getString("id");

        // Create a new route model with actions
        json = target().path("/routemodel").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("name", "Workflow action 1")
                        .param("steps", "[{\"type\":\"APPROVE\",\"transitions\":[{\"name\":\"APPROVED\",\"actions\":[{\"type\":\"ADD_TAG\",\"tag\":\"" + tagApprovedId + "\"}]},{\"name\":\"REJECTED\",\"actions\":[]}],\"target\":{\"name\":\"administrators\",\"type\":\"GROUP\"},\"name\":\"Check the document's metadata\"},{\"type\":\"VALIDATE\",\"transitions\":[{\"name\":\"VALIDATED\",\"actions\":[{\"type\":\"REMOVE_TAG\",\"tag\":\"" + tagPendingId + "\"}]}],\"target\":{\"name\":\"administrators\",\"type\":\"GROUP\"},\"name\":\"Check the document's metadata\"}]")), JsonObject.class);
        String routeModelId = json.getString("id");

        // Create a document
        json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("title", "My super title document 1")
                        .param("description", "My super description for document 1")
                        .param("tags", tagPendingId)
                        .param("language", "eng")), JsonObject.class);
        String document1Id = json.getString("id");

        // Start the route on document 1
        json = target().path("/route/start").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("routeModelId", routeModelId)), JsonObject.class);
        JsonObject step = json.getJsonObject("route_step");
        Assert.assertEquals("Check the document's metadata", step.getString("name"));

        // Check tags on document 1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        JsonArray tags = json.getJsonArray("tags");
        Assert.assertEquals(1, tags.size());
        Assert.assertEquals(tagPendingId, tags.getJsonObject(0).getString("id"));

        // Validate the current step with admin
        target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("transition", "APPROVED")), JsonObject.class);

        // Check tags on document 1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertEquals(2, tags.size());
        Assert.assertEquals(tagApprovedId, tags.getJsonObject(0).getString("id"));
        Assert.assertEquals(tagPendingId, tags.getJsonObject(1).getString("id"));

        // Validate the current step with admin
        target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document1Id)
                        .param("transition", "VALIDATED")), JsonObject.class);

        // Check tags on document 1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertEquals(1, tags.size());
        Assert.assertEquals(tagApprovedId, tags.getJsonObject(0).getString("id"));

        // Create a document
        json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("title", "My super title document 2")
                        .param("tags", tagPendingId)
                        .param("language", "eng")), JsonObject.class);
        String document2Id = json.getString("id");

        // Start the route on document 2
        json = target().path("/route/start").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document2Id)
                        .param("routeModelId", routeModelId)), JsonObject.class);
        step = json.getJsonObject("route_step");
        Assert.assertEquals("Check the document's metadata", step.getString("name"));

        // Validate the current step with admin
        target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document2Id)
                        .param("transition", "REJECTED")), JsonObject.class);

        // Check tags on document 2
        json = target().path("/document/" + document2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertEquals(1, tags.size());
        Assert.assertEquals(tagPendingId, tags.getJsonObject(0).getString("id"));

        // Validate the current step with admin
        target().path("/route/validate").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("documentId", document2Id)
                        .param("transition", "VALIDATED")), JsonObject.class);

        // Check tags on document 2
        json = target().path("/document/" + document2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertEquals(0, tags.size());

        // Delete the documents
        target().path("/document/" + document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete(JsonObject.class);
        target().path("/document/" + document2Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete(JsonObject.class);

        // Delete the route model
        target().path("/routemodel/" + routeModelId)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete(JsonObject.class);
    }
}