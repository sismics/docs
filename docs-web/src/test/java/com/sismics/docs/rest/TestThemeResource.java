package com.sismics.docs.rest;

import com.google.common.io.Resources;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

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
    public void testThemeResource() throws Exception {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);

        // Get the stylesheet anonymously
        String stylesheet = target().path("/theme/stylesheet").request()
                .get(String.class);
        Assert.assertTrue(stylesheet.contains("background-color: #ffffff;"));

        // Get the theme configuration anonymously
        JsonObject json = target().path("/theme").request()
                .get(JsonObject.class);
        Assert.assertEquals("Teedy", json.getString("name"));
        Assert.assertEquals("#ffffff", json.getString("color"));
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

        // Get the logo
        Response response = target().path("/theme/image/logo").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Get the background
        response = target().path("/theme/image/background").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Change the logo
        try (InputStream is = Resources.getResource("file/PIA00452.jpg").openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("image", is, "PIA00452.jpg");
            try (FormDataMultiPart multiPart = new FormDataMultiPart()) {
                target()
                        .register(MultiPartFeature.class)
                        .path("/theme/image/logo").request()
                        .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                        .put(Entity.entity(multiPart.bodyPart(streamDataBodyPart),
                                MediaType.MULTIPART_FORM_DATA_TYPE), JsonObject.class);
            }
        }

        // Change the background
        try (InputStream is = Resources.getResource("file/Einstein-Roosevelt-letter.png").openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("image", is, "Einstein-Roosevelt-letter.png");
            try (FormDataMultiPart multiPart = new FormDataMultiPart()) {
                target()
                        .register(MultiPartFeature.class)
                        .path("/theme/image/background").request()
                        .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                        .put(Entity.entity(multiPart.bodyPart(streamDataBodyPart),
                                MediaType.MULTIPART_FORM_DATA_TYPE), JsonObject.class);
            }
        }

        // Get the logo
        response = target().path("/theme/image/logo").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Get the background
        response = target().path("/theme/image/background").request().get();
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
}