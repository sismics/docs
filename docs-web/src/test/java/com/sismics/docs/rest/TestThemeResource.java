package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

/**
 * Test the theme resource.
 * 
 * @author bgamard
 */
public class TestThemeResource extends BaseJerseyTest {
    /**
     * Test the theme resource.
     */
    @Test
    public void testThemeResource() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);

        // Get the stylesheet anonymously
        String stylesheet = target().path("/theme/stylesheet").request()
                .get(String.class);
        Assert.assertTrue(stylesheet.contains("background-color: #263238;"));

        // Get the theme configuration anonymously
        JsonObject json = target().path("/theme").request()
                .get(JsonObject.class);
        Assert.assertEquals("Sismics Docs", json.getString("name"));
        Assert.assertEquals("#263238", json.getString("color"));
        Assert.assertEquals("", json.getString("css"));

        // Update the main color as admin
        target().path("/theme").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("color", "#ff0000")
                .param("name", "My App")
                .param("css", ".body { content: 'Custom CSS'; }")), JsonObject.class);

        // Get the stylesheet anonymously
        stylesheet = target().path("/theme/stylesheet").request()
                .get(String.class);
        Assert.assertTrue(stylesheet.contains("background-color: #ff0000;"));
        Assert.assertTrue(stylesheet.contains("Custom CSS"));

        // Get the theme configuration anonymously
        json = target().path("/theme").request()
                .get(JsonObject.class);
        Assert.assertEquals("My App", json.getString("name"));
        Assert.assertEquals("#ff0000", json.getString("color"));
        Assert.assertEquals(".body { content: 'Custom CSS'; }", json.getString("css"));
    }
}