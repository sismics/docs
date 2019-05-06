package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import com.sismics.util.totp.GoogleAuthenticator;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Exhaustive test of the user resource.
 * 
 * @author jtremeaux
 */
public class TestUserResource extends BaseJerseyTest {
    /**
     * Test the user resource.
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
        String adminToken = clientUtil.login("admin", "admin", false);
        
        // List all users
        json = target().path("/user/list")
                .queryParam("sort_column", 2)
                .queryParam("asc", false)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        JsonArray users = json.getJsonArray("users");
        Assert.assertTrue(users.size() > 0);
        JsonObject user = users.getJsonObject(0);
        Assert.assertNotNull(user.getString("id"));
        Assert.assertNotNull(user.getString("username"));
        Assert.assertNotNull(user.getString("email"));
        Assert.assertNotNull(user.getJsonNumber("storage_quota"));
        Assert.assertNotNull(user.getJsonNumber("storage_current"));
        Assert.assertNotNull(user.getJsonNumber("create_date"));
        Assert.assertFalse(user.getBoolean("totp_enabled"));
        Assert.assertFalse(user.getBoolean("disabled"));

        // Create a user KO (login length validation)
        Response response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
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
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
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
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
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
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
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
        target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(form), JsonObject.class);

        // Create a user bob KO : duplicate username
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(form));
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
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
        String bobToken = clientUtil.login("bob");
        String bobToken2 = clientUtil.login("bob");

        // List sessions
        response = target().path("/user/session").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobToken)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertTrue(json.getJsonArray("sessions").size() > 0);
        JsonObject session = json.getJsonArray("sessions").getJsonObject(0);
        Assert.assertEquals("127.0.0.1", session.getString("ip"));
        Assert.assertTrue(session.getString("user_agent").startsWith("Jersey"));
        
        // Delete all sessions
        response = target().path("/user/session").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobToken)
                .delete();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Check bob user information with token 2 (just deleted)
        response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobToken2)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertTrue(json.getBoolean("anonymous"));
        
        // Check alice user information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, aliceAuthToken)
                .get(JsonObject.class);
        Assert.assertEquals("alice@docs.com", json.getString("email"));
        Assert.assertFalse(json.getBoolean("is_default_password"));
        Assert.assertEquals(0L, json.getJsonNumber("storage_current").longValue());
        Assert.assertEquals(1000000L, json.getJsonNumber("storage_quota").longValue());
        
        // Check bob user information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobToken)
                .get(JsonObject.class);
        Assert.assertTrue(json.getBoolean("onboarding"));
        Assert.assertEquals("bob@docs.com", json.getString("email"));

        // Pass onboarding
        target().path("/user/onboarded").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobToken)
                .post(Entity.form(new Form()), JsonObject.class);

        // Check bob user information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, bobToken)
                .get(JsonObject.class);
        Assert.assertFalse(json.getBoolean("onboarding"));

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
     */
    @Test
    public void testUserResourceAdmin() {
        // Create admin_user1 user
        clientUtil.createUser("admin_user1");

        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);

        // Check admin information
        JsonObject json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        Assert.assertTrue(json.getBoolean("is_default_password"));
        Assert.assertEquals(0L, json.getJsonNumber("storage_current").longValue());
        Assert.assertEquals(10000000000L, json.getJsonNumber("storage_quota").longValue());

        // User admin updates his information
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("email", "newadminemail@docs.com")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check admin information update
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        Assert.assertEquals("newadminemail@docs.com", json.getString("email"));

        // User admin update admin_user1 information
        json = target().path("/user/admin_user1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("email", " alice2@docs.com ")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // User admin deletes himself: forbidden
        Response response = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ForbiddenError", json.getString("type"));

        // User admin disable admin_user1
        json = target().path("/user/admin_user1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("disabled", "true")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // User admin_user1 tries to authenticate
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "admin_user1")
                        .param("password", "12345678")
                        .param("remember", "false")));
        Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());

        // User admin enable admin_user1
        json = target().path("/user/admin_user1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("disabled", "false")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // User admin_user1 tries to authenticate
        response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "admin_user1")
                        .param("password", "12345678")
                        .param("remember", "false")));
        Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

        // User admin deletes user admin_user1
        json = target().path("/user/admin_user1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // User admin deletes user admin_user1 : KO (user doesn't exist)
        response = target().path("/user/admin_user1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("UserNotFound", json.getString("type"));
    }
    
    @Test
    public void testTotp() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);

        // Create totp1 user
        clientUtil.createUser("totp1");
        String totp1Token = clientUtil.login("totp1");
        
        // Check TOTP enablement
        JsonObject json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, totp1Token)
                .get(JsonObject.class);
        Assert.assertFalse(json.getBoolean("totp_enabled"));
        
        // Enable TOTP for totp1
        json = target().path("/user/enable_totp").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, totp1Token)
                .post(Entity.form(new Form()), JsonObject.class);
        String secret = json.getString("secret");
        Assert.assertNotNull(secret);
        
        // Try to login with totp1 without a validation code
        Response response = target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "totp1")
                        .param("password", "12345678")
                        .param("remember", "false")));
        Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationCodeRequired", json.getString("type"));
        
        // Generate a OTP
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        int validationCode = googleAuthenticator.calculateCode(secret, new Date().getTime() / 30000);
        
        // Login with totp1 with a validation code
        target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "totp1")
                        .param("password", "12345678")
                        .param("code", Integer.toString(validationCode))
                        .param("remember", "false")), JsonObject.class);
        
        // Check TOTP enablement
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, totp1Token)
                .get(JsonObject.class);
        Assert.assertTrue(json.getBoolean("totp_enabled"));

        // Generate a OTP
        validationCode = googleAuthenticator.calculateCode(secret, new Date().getTime() / 30000);

        // Test a validation code
        target().path("/user/test_totp").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, totp1Token)
                .post(Entity.form(new Form()
                        .param("code", Integer.toString(validationCode))), JsonObject.class);

        // Disable TOTP for totp1
        target().path("/user/disable_totp").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, totp1Token)
                .post(Entity.form(new Form()
                        .param("password", "12345678")), JsonObject.class);

        // Enable TOTP for totp1
        target().path("/user/enable_totp").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, totp1Token)
                .post(Entity.form(new Form()), JsonObject.class);

        // Disable TOTP for totp1 with admin
        target().path("/user/totp1/disable_totp").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()), JsonObject.class);

        // Login with totp1 without a validation code
        target().path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", "totp1")
                        .param("password", "12345678")
                        .param("remember", "false")), JsonObject.class);
        
        // Check TOTP enablement
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, totp1Token)
                .get(JsonObject.class);
        Assert.assertFalse(json.getBoolean("totp_enabled"));
    }

    @Test
    public void testResetPassword() throws Exception {
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

        // Create absent_minded who lost his password
        clientUtil.createUser("absent_minded");

        // User no_such_user try to recovery its password: invalid user
        Response response = target().path("/user/password_lost").request()
                .post(Entity.form(new Form()
                        .param("username", "no_such_user")));
        Assert.assertEquals(Response.Status.BAD_REQUEST, Response.Status.fromStatusCode(response.getStatus()));
        JsonObject json = response.readEntity(JsonObject.class);
        Assert.assertEquals("UserNotFound", json.getString("type"));

        // User absent_minded try to recovery its password: OK
        json = target().path("/user/password_lost").request()
                .post(Entity.form(new Form()
                        .param("username", "absent_minded")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        String emailBody = popEmail();
        Assert.assertNotNull("No email to consume", emailBody);
        Assert.assertTrue(emailBody.contains("Please reset your password"));
        Pattern keyPattern = Pattern.compile("/passwordreset/(.+?)\"");
        Matcher keyMatcher = keyPattern.matcher(emailBody);
        Assert.assertTrue("Token not found", keyMatcher.find());
        String key = keyMatcher.group(1).replaceAll("=", "");

        // User absent_minded resets its password: invalid key
        response = target().path("/user/password_reset").request()
                .post(Entity.form(new Form()
                        .param("key", "no_such_key")
                        .param("password", "87654321")));
        Assert.assertEquals(Response.Status.BAD_REQUEST, Response.Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("KeyNotFound", json.getString("type"));

        // User absent_minded resets its password: password invalid
        response = target().path("/user/password_reset").request()
                .post(Entity.form(new Form()
                        .param("key", key)
                        .param("password", " 1 ")));
        Assert.assertEquals(Response.Status.BAD_REQUEST, Response.Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ValidationError", json.getString("type"));
        Assert.assertTrue(json.getString("message"), json.getString("message").contains("password"));

        // User absent_minded resets its password: OK
        json = target().path("/user/password_reset").request()
                .post(Entity.form(new Form()
                        .param("key", key)
                        .param("password", "87654321")), JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // User absent_minded resets its password: expired key
        response = target().path("/user/password_reset").request()
                .post(Entity.form(new Form()
                        .param("key", key)
                        .param("password", "87654321")));
        Assert.assertEquals(Response.Status.BAD_REQUEST, Response.Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("KeyNotFound", json.getString("type"));
    }
}