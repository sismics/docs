package com.sismics.docs.rest;

import java.util.Locale;

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
 * Exhaustive test of the user resource.
 * 
 * @author jtremeaux
 */
public class TestUserResource extends BaseJerseyTest {
    /**
     * Test the user resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testUserResource() {
        // Check anonymous user information
        JsonObject json = target().path("/user").request()
                .acceptLanguage(Locale.US)
                .get(JsonObject.class);
        Assert.assertTrue(json.getBoolean("is_default_password"));
        
        // Create alice user
        clientUtil.createUser("alice");

        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);
        
        // List all users
        json = target().path("/user/list")
                .queryParam("sort_column", 2)
                .queryParam("asc", false)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        JsonArray users = json.getJsonArray("users");
        Assert.assertTrue(users.size() > 0);
        
        // Create a user KO (login length validation)
        Response response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("username", "   bb  ")
                        .param("email", "bob@docs.com")
                        .param("password", "12345678")
                        .param("storage_quota", "10")));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("more than 3"));

        // Create a user KO (login format validation)
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("username", "bob-")
                        .param("email", "bob@docs.com")
                        .param("password", "12345678")
                        .param("storage_quota", "10")));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("alphanumeric"));
        
        // Create a user KO (invalid quota)
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("username", "bob")
                        .param("email", "bob@docs.com")
                        .param("password", "12345678")
                        .param("storage_quota", "nope")));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("number"));

        // Create a user KO (email format validation)
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(new Form()
                        .param("username", "bob")
                        .param("email", "bobdocs.com")
                        .param("password", "12345678")
                        .param("storage_quota", "10")));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("must be an email"));

        // Create a user bob OK
        Form form = new Form()
                .param("username", " bob ")
                .param("email", " bob@docs.com ")
                .param("password", " 12345678 ")
                .param("storage_quota", "10");
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(form), JsonObject.class);

        // Create a user bob KO : duplicate username
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .put(Entity.form(form));
        Assert.assertNotSame(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("AlreadyExistingUsername", json.getString("type"));

        // Check if a username is free : OK
        target().path("/user/check_username").queryParam("username", "carol").request().get(JsonObject.class);

        // Check if a username is free : KO
        response = target().path("/user/check_username").queryParam("username", "alice").request().get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ko", json.getString("status"));

        // Login alice with extra whitespaces
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", " alice ")
                        .param("password", " 12345678 ")));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        String aliceAuthToken = clientUtil.getAuthenticationCookie(response);

        // Login user bob twice
        String bobAuthToken = clientUtil.login("bob");
        String bobAuthToken2 = clientUtil.login("bob");

        // List sessions
        response = target().path("/user/session").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobAuthToken)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertTrue(json.getJsonArray("sessions").size() > 0);
        JsonObject session = json.getJsonArray("sessions").getJsonObject(0);
        Assert.assertEquals("127.0.0.1", session.getString("ip"));
        Assert.assertTrue(session.getString("user_agent").startsWith("Jersey"));
        
        // Delete all sessions
        response = target().path("/user/session").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobAuthToken)
                .delete();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Check bob user information with token 2 (just deleted)
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobAuthToken2)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals(true, json.getBoolean("anonymous"));
        
        // Check alice user information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthToken)
                .get(JsonObject.class);
        Assert.assertEquals("alice@docs.com", json.getString("email"));
        Assert.assertFalse(json.getBoolean("is_default_password"));
        Assert.assertEquals(0l, json.getJsonNumber("storage_current").longValue());
        Assert.assertEquals(1000000l, json.getJsonNumber("storage_quota").longValue());
        
        // Check bob user information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobAuthToken)
                .get(JsonObject.class);
        Assert.assertEquals("bob@docs.com", json.getString("email"));
        
        // Test login KO (user not found)
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "intruder")
                        .param("password", "12345678")));
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // Test login KO (wrong password)
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "alice")
                        .param("password", "error")));
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // User alice updates her information + changes her email
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthToken)
                .post(Entity.form(new Form()
                        .param("email", " alice2@docs.com ")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Check the update
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthToken)
                .get(JsonObject.class);
        Assert.assertEquals("alice2@docs.com", json.getString("email"));
        
        // Delete user alice
        target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthToken)
                .delete();
        
        // Check the deletion
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "alice")
                        .param("password", "12345678")));
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
    }

    /**
     * Test the user resource admin functions.
     * 
     * @throws JSONException
     */
    @Test
    public void testUserResourceAdmin() {
        // Create admin_user1 user
        clientUtil.createUser("admin_user1");

        // Login admin
        String adminAuthenticationToken = clientUtil.login("admin", "admin", false);

        // Check admin information
        JsonObject json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertTrue(json.getBoolean("is_default_password"));
        Assert.assertEquals(0l, json.getJsonNumber("storage_current").longValue());
        Assert.assertEquals(10000000000l, json.getJsonNumber("storage_quota").longValue());

        // User admin updates his information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("email", "newadminemail@docs.com")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check admin information update
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .get(JsonObject.class);
        Assert.assertEquals("newadminemail@docs.com", json.getString("email"));

        // User admin update admin_user1 information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .post(Entity.form(new Form()
                        .param("email", " alice2@docs.com ")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // User admin deletes himself: forbidden
        Response response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ForbiddenError", json.getString("type"));

        // User admin deletes user admin_user1
        json = target().path("/user/admin_user1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // User admin deletes user admin_user1 : KO (user doesn't exist)
        response = target().path("/user/admin_user1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminAuthenticationToken)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("UserNotFound", json.getString("type"));
    }
}