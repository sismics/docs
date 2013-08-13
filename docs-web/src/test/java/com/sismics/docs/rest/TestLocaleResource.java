package com.sismics.docs.rest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
    public void testLocaleResource() throws JSONException {
        WebResource localeResource = resource().path("/locale");
        ClientResponse response = localeResource.get(ClientResponse.class);
        response = localeResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        JSONArray locale = json.getJSONArray("locales");
        Assert.assertTrue(locale.length() > 0);
    }
}