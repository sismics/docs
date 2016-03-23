package com.sismics.docs.rest;

import java.util.Date;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;

import com.sismics.util.filter.TokenBasedSecurityFilter;

/**
 * Exhaustive test of the comment resource.
 * 
 * @author bgamard
 */
public class TestCommentResource extends BaseJerseyTest {
    /**
     * Test the comment resource.
     */
    @Test
    public void testCommentResource() {
        // Login comment1
        clientUtil.createUser("comment1");
        String comment1Token = clientUtil.login("comment1");
        
        // Login comment2
        clientUtil.createUser("comment2");
        String comment2Token = clientUtil.login("comment2");
        
        // Create a document with comment1
        long create1Date = new Date().getTime();
        JsonObject json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment1Token)
                .put(Entity.form(new Form()
                        .param("title", "My super title document 1")
                        .param("description", "My super description for document 1")
                        .param("language", "eng")
                        .param("create_date", Long.toString(create1Date))), JsonObject.class);
        String document1Id = json.getString("id");
        Assert.assertNotNull(document1Id);
        
        // Create a comment with comment2 (fail, no read access)
        Response response = target().path("/comment").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment2Token)
                .put(Entity.form(new Form()
                        .param("id", document1Id)
                        .param("content", "Comment by comment2")));
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Read comments with comment2 (fail, no read access)
        response = target().path("/comment/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment2Token)
                .get();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Read comments with comment 1
        json = target().path("/comment/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment1Token)
                .get(JsonObject.class);
        Assert.assertEquals(0, json.getJsonArray("comments").size());
        
        // Create a comment with comment1
        json = target().path("/comment").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment1Token)
                .put(Entity.form(new Form()
                        .param("id", document1Id)
                        .param("content", "Comment by comment1")), JsonObject.class);
        String comment1Id = json.getString("id");
        Assert.assertNotNull(comment1Id);
        Assert.assertEquals("Comment by comment1", json.getString("content"));
        Assert.assertEquals("comment1", json.getString("creator"));
        Assert.assertNotNull(json.getJsonNumber("create_date"));
        
        // Read comments with comment1
        json = target().path("/comment/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment1Token)
                .get(JsonObject.class);
        Assert.assertEquals(1, json.getJsonArray("comments").size());
        Assert.assertEquals(comment1Id, json.getJsonArray("comments").getJsonObject(0).getString("id"));
        
        // Delete a comment with comment2 (fail, no write access)
        response = target().path("/comment/" + comment1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment2Token)
                .delete();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Delete a comment with comment1
        json = target().path("/comment/" + comment1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment1Token)
                .delete(JsonObject.class);
        
        // Read comments with comment1
        json = target().path("/comment/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment1Token)
                .get(JsonObject.class);
        Assert.assertEquals(0, json.getJsonArray("comments").size());
        
        // Add an ACL READ for comment2 with comment1
        json = target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "READ")
                        .param("target", "comment2")
                        .param("type", "USER")), JsonObject.class);
        
        // Create a comment with comment2
        json = target().path("/comment").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment2Token)
                .put(Entity.form(new Form()
                        .param("id", document1Id)
                        .param("content", "Comment by comment2")), JsonObject.class);
        String comment2Id = json.getString("id");
        
        // Read comments with comment2
        json = target().path("/comment/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment2Token)
                .get(JsonObject.class);
        Assert.assertEquals(1, json.getJsonArray("comments").size());
        JsonObject comment = json.getJsonArray("comments").getJsonObject(0);
        Assert.assertEquals(comment2Id, comment.getString("id"));
        Assert.assertEquals("Comment by comment2", comment.getString("content"));
        Assert.assertEquals("comment2", comment.getString("creator"));
        Assert.assertEquals("d6e56c42f61983bba80d370138763420", comment.getString("creator_gravatar"));
        Assert.assertNotNull(comment.getJsonNumber("create_date"));
        
        // Delete a comment with comment2
        json = target().path("/comment/" + comment2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment2Token)
                .delete(JsonObject.class);
        
        // Read comments with comment2
        json = target().path("/comment/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, comment2Token)
                .get(JsonObject.class);
        Assert.assertEquals(0, json.getJsonArray("comments").size());
    }
}