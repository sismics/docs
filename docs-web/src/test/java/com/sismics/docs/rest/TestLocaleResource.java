package com.sismics.docs.rest;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.Test;


/**
 * Test the locale resource.
 * 
 * @author jtremeaux
 */
public class TestLocaleResource extends BaseJerseyTest {
    /**
     * Test the locale resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testLocaleResource() {
        JsonObject json = target().path("/locale").request().get(JsonObject.class);
        JsonArray locale = json.getJsonArray("locales");
        Assert.assertTrue(locale.size() > 0);
    }
}