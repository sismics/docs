package com.sismics.docs.rest;

import java.util.Date;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;

import com.sismics.util.filter.TokenBasedSecurityFilter;


/**
 * Test the ACL resource.
 * 
 * @author bgamard
 */
public class TestAclResource extends BaseJerseyTest {
    /**
     * Test the ACL resource.
     */
    @Test
    public void testAclResource() {
        // Create aclGroup2
        clientUtil.createGroup("aclGroup2");
        
        // Login acl1
        clientUtil.createUser("acl1");
        String acl1Token = clientUtil.login("acl1");
        
        // Login acl2
        clientUtil.createUser("acl2", "aclGroup2");
        String acl2Token = clientUtil.login("acl2");
        
        // Create a document with acl1
        JsonObject json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("title", "My super title document 1")
                        .param("language", "eng")
                        .param("create_date", Long.toString(new Date().getTime()))), JsonObject.class);
        String document1Id = json.getString("id");
        
        // Get the document as acl1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        JsonArray acls = json.getJsonArray("acls");
        Assert.assertEquals(2, acls.size());
        
        // Get the document as acl2
        Response response = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Add an ACL READ for acl2 with acl1
        json = target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "READ")
                        .param("target", "acl2")
                        .param("type", "USER")), JsonObject.class);
        String acl2Id = json.getString("id");
        
        // Add an ACL WRITE for acl2 with acl1
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "WRITE")
                        .param("target", "acl2")
                        .param("type", "USER")), JsonObject.class);
        
        // Add an ACL WRITE for acl2 with acl1 (again)
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "WRITE")
                        .param("target", "acl2")
                        .param("type", "USER")), JsonObject.class);
        
        // Add an ACL READ for aclGroup2 with acl1
        json = target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "READ")
                        .param("target", "aclGroup2")
                        .param("type", "GROUP")), JsonObject.class);
        String aclGroup2Id = json.getString("id");
        
        // Add an ACL WRITE for aclGroup2 with acl1
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "WRITE")
                        .param("target", "aclGroup2")
                        .param("type", "GROUP")), JsonObject.class);
        
        // List all documents with acl2
        json = target().path("/document/list")
                .queryParam("sort_column", 3)
                .queryParam("asc", true)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        JsonArray documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());
        
        // Get the document as acl1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(6, acls.size());
        Assert.assertTrue(json.getBoolean("writable"));
        
        // Get the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(6, acls.size());
        Assert.assertTrue(json.getBoolean("writable"));
        
        // Update the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .post(Entity.form(new Form()
                        .param("title", "My new super document 1")
                        .param("language", "eng")), JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        
        // Get the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        JsonArray contributors = json.getJsonArray("contributors");
        Assert.assertEquals(2, contributors.size());
        
        // Delete the ACL WRITE for acl2 with acl2
        target().path("/acl/" + document1Id + "/WRITE/" + acl2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .delete(JsonObject.class);
        
        // Get the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(5, acls.size());
        Assert.assertTrue(json.getBoolean("writable")); // Writable by aclGroup2
        
        // Delete the ACL WRITE for aclGroup2 with acl2
        target().path("/acl/" + document1Id + "/WRITE/" + aclGroup2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .delete(JsonObject.class);
        
        // Get the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(4, acls.size());
        Assert.assertFalse(json.getBoolean("writable"));
        
        // Delete the ACL READ for acl2 with acl2 (not authorized)
        response = target().path("/acl/" + document1Id + "/READ/" + acl2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .delete();
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
        
        // Delete the ACL READ for acl2 with acl1
        target().path("/acl/" + document1Id + "/READ/" + acl2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .delete(JsonObject.class);
        
        // Get the document as acl2 (visible by group)
        target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        
        // Delete the ACL READ for aclGroup2 with acl1
        target().path("/acl/" + document1Id + "/READ/" + aclGroup2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .delete(JsonObject.class);
        
        // Get the document as acl1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(2, acls.size());
        String acl1Id = acls.getJsonObject(0).getString("id");
        
        // Get the document as acl2
        response = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Delete the ACL READ for acl1 with acl1
        response = target().path("/acl/" + document1Id + "/READ/" + acl1Id).request()
            .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
            .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        
        // Delete the ACL WRITE for acl1 with acl1
        response = target().path("/acl/" + document1Id + "/WRITE/" + acl1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        
        // Search target list (acl)
        json = target().path("/acl/target/search")
                .queryParam("search", "acl")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        JsonArray users = json.getJsonArray("users");
        Assert.assertEquals(2, users.size());
        JsonArray groups = json.getJsonArray("groups");
        Assert.assertEquals(1, groups.size());
        
        // Search target list (admin)
        json = target().path("/acl/target/search")
                .queryParam("search", "admin")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        users = json.getJsonArray("users");
        Assert.assertEquals(1, users.size());
        groups = json.getJsonArray("groups");
        Assert.assertEquals(1, groups.size());
    }
}