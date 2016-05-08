package com.sismics.docs.rest;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;

import com.sismics.util.filter.TokenBasedSecurityFilter;

/**
 * Test the tag resource.
 * 
 * @author bgamard
 */
public class TestTagResource extends BaseJerseyTest {
    /**
     * Test the tag resource.
     */
    @Test
    public void testTagResource() {
        // Login tag1
        clientUtil.createUser("tag1");
        String tag1Token = clientUtil.login("tag1");
        
        // Create a tag
        JsonObject json = target().path("/tag").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .put(Entity.form(new Form()
                        .param("name", "Tag3")
                        .param("color", "#ff0000")), JsonObject.class);
        String tag3Id = json.getString("id");
        Assert.assertNotNull(tag3Id);
        
        // Create a tag
        json = target().path("/tag").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .put(Entity.form(new Form()
                        .param("name", "Tag4")
                        .param("color", "#00ff00")
                        .param("parent", tag3Id)), JsonObject.class);
        String tag4Id = json.getString("id");
        Assert.assertNotNull(tag4Id);

        // Get the tag
        json = target().path("/tag/" + tag4Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .get(JsonObject.class);
        Assert.assertEquals("Tag4", json.getString("name"));
        Assert.assertEquals("tag1", json.getString("creator"));
        Assert.assertEquals("#00ff00", json.getString("color"));
        Assert.assertEquals(tag3Id, json.getString("parent"));
        Assert.assertTrue(json.getBoolean("writable"));
        JsonArray acls = json.getJsonArray("acls");
        Assert.assertEquals(2, acls.size());
        
        // Create a tag with space (not allowed)
        Response response = target().path("/tag").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .put(Entity.form(new Form()
                        .param("name", "Tag 4")));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        
        // Create a document
        target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .put(Entity.form(new Form()
                        .param("title", "My super document 1")
                        .param("tags", tag3Id)
                        .param("language", "eng")), JsonObject.class);
        
        // Create a document
        json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .put(Entity.form(new Form()
                        .param("title", "My super document 2")
                        .param("tags", tag4Id)
                        .param("language", "eng")), JsonObject.class);
        String document2Id = json.getString("id");
        
        // Check tags on a document
        json = target().path("/document/" + document2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .get(JsonObject.class);
        JsonArray tags = json.getJsonArray("tags");
        Assert.assertEquals(1, tags.size());
        Assert.assertEquals(tag4Id, tags.getJsonObject(0).getString("id"));
        
        // Update tags on a document
        response = target().path("/document/" + document2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .post(Entity.form(new Form()
                        .param("title", "My super document 2")
                        .param("language", "eng")
                        .param("tags", tag3Id)
                        .param("tags", tag4Id)));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Check tags on a document
        json = target().path("/document/" + document2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertEquals(2, tags.size());
        Assert.assertEquals(tag3Id, tags.getJsonObject(0).getString("id"));
        Assert.assertEquals(tag4Id, tags.getJsonObject(1).getString("id"));
        
        // Update tags on a document
        response = target().path("/document/" + document2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .post(Entity.form(new Form()
                        .param("title", "My super document 2")
                        .param("language", "eng")
                        .param("tags", tag4Id)));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Check tags on a document
        json = target().path("/document/" + document2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertEquals(1, tags.size());
        Assert.assertEquals(tag4Id, tags.getJsonObject(0).getString("id"));
        
        // Get all tags
        json = target().path("/tag/list").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertTrue(tags.size() > 0);
        Assert.assertEquals("Tag4", tags.getJsonObject(1).getString("name"));
        Assert.assertEquals("#00ff00", tags.getJsonObject(1).getString("color"));
        Assert.assertEquals(tag3Id, tags.getJsonObject(1).getString("parent"));
        
        // Update a tag
        json = target().path("/tag/" + tag4Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .post(Entity.form(new Form()
                        .param("name", "UpdatedName")
                        .param("color", "#0000ff")), JsonObject.class);
        Assert.assertEquals(tag4Id, json.getString("id"));
        
        // Get all tags
        json = target().path("/tag/list").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertTrue(tags.size() > 0);
        Assert.assertEquals("UpdatedName", tags.getJsonObject(1).getString("name"));
        Assert.assertEquals("#0000ff", tags.getJsonObject(1).getString("color"));
        Assert.assertNull(tags.getJsonObject(1).get("parent"));
        
        // Deletes a tag
        target().path("/tag/" + tag4Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .delete();
        
        // Get all tags
        json = target().path("/tag/list").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, tag1Token)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertTrue(tags.size() == 1);
    }
}