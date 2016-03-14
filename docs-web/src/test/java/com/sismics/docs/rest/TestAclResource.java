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
     * 
     * @throws JSONException
     */
    @Test
    public void testAclResource() {
        // Login acl1
        clientUtil.createUser("acl1");
        String acl1Token = clientUtil.login("acl1");
        
        // Login acl2
        clientUtil.createUser("acl2");
        String acl2Token = clientUtil.login("acl2");
        
        // Create a document
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
                        .param("username", "acl2")), JsonObject.class);
        String acl2Id = json.getString("id");
        
        // Add an ACL WRITE for acl2 with acl1
        json = target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "WRITE")
                        .param("username", "acl2")), JsonObject.class);
        
        // Add an ACL WRITE for acl2 with acl1 (again)
        json = target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "WRITE")
                        .param("username", "acl2")), JsonObject.class);
        
        // Get the document as acl1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(4, acls.size());
        
        // Get the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(4, acls.size());
        
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
        
        // Delete the ACL READ for acl2 with acl2 (not authorized)
        response = target().path("/acl/" + document1Id + "/READ/" + acl2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .delete();
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
        
        // Delete the ACL READ for acl2 with acl1
        target().path("/acl/" + document1Id + "/READ/" + acl2Id).request()
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
        
        // Search target list
        json = target().path("/acl/target/search")
                .queryParam("search", "acl")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        JsonArray users = json.getJsonArray("users");
        Assert.assertEquals(2, users.size());
    }
}