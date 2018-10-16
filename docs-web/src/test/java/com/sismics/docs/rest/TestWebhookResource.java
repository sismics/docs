package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;


/**
 * Test the webhook resource.
 * 
 * @author bgamard
 */
public class TestWebhookResource extends BaseJerseyTest {
    /**
     * Test the webhook resource.
     */
    @Test
    public void testWebhookResource() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);
        
        // Get all webhooks
        JsonObject json = target().path("/webhook")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        JsonArray webhooks = json.getJsonArray("webhooks");
        Assert.assertEquals(0, webhooks.size());

        // Create a webhook
        target().path("/webhook").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("event", "DOCUMENT_CREATED")
                        .param("url", "https://www.sismics.com")), JsonObject.class);

        // Get all webhooks
        json = target().path("/webhook")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        webhooks = json.getJsonArray("webhooks");
        Assert.assertEquals(1, webhooks.size());
        JsonObject webhook = webhooks.getJsonObject(0);
        String webhookId = webhook.getString("id");
        Assert.assertEquals("DOCUMENT_CREATED", webhook.getString("event"));
        Assert.assertEquals("https://www.sismics.com", webhook.getString("url"));
        Assert.assertNotNull(webhook.getJsonNumber("create_date"));

        // Delete a webhook
        target().path("/webhook/" + webhookId).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete(JsonObject.class);

        // Get all webhooks
        json = target().path("/webhook")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        webhooks = json.getJsonArray("webhooks");
        Assert.assertEquals(0, webhooks.size());
    }
}