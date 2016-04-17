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
        Assert.assertTrue(stylesheet.contains("background-color: inherit;"));

        // Update the main color as admin
        target().path("/theme/color").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("color", "#ff0000")), JsonObject.class);

        // Get the stylesheet anonymously
        stylesheet = target().path("/theme/stylesheet").request()
                .get(String.class);
        Assert.assertTrue(stylesheet.contains("background-color: #ff0000;"));
    }
}