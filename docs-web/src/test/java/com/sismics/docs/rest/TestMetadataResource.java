package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;


/**
 * Test the metadata resource.
 *
 * @author bgamard
 */
public class TestMetadataResource extends BaseJerseyTest {
    /**
     * Test the metadata resource.
     */
    @Test
    public void testMetadataResource() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);

        // Get all metadata with admin
        JsonObject json = target().path("/metadata")
                .queryParam("sort_column", "2")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        JsonArray metadata = json.getJsonArray("metadata");
        Assert.assertEquals(0, metadata.size());

        // Create a metadata with admin
        json = target().path("/metadata").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("name", "ISBN 13")
                        .param("type", "STRING")), JsonObject.class);
        String metadataIsbnId = json.getString("id");
        Assert.assertNotNull(metadataIsbnId);
        Assert.assertEquals("ISBN 13", json.getString("name"));
        Assert.assertEquals("STRING", json.getString("type"));

        // Get all metadata with admin
        json = target().path("/metadata")
                .queryParam("sort_column", "2")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        metadata = json.getJsonArray("metadata");
        Assert.assertEquals(1, metadata.size());

        // Update a metadata with admin
        json = target().path("/metadata/" + metadataIsbnId).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("name", "ISBN 10")), JsonObject.class);
        Assert.assertEquals(metadataIsbnId, json.getString("id"));
        Assert.assertEquals("ISBN 10", json.getString("name"));
        Assert.assertEquals("STRING", json.getString("type"));

        // Delete a metadata with admin
        target().path("/metadata/" + metadataIsbnId).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete(JsonObject.class);

        // Get all metadata with admin
        json = target().path("/metadata")
                .queryParam("sort_column", "2")
                .queryParam("asc", "false")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        metadata = json.getJsonArray("metadata");
        Assert.assertEquals(0, metadata.size());
    }
}