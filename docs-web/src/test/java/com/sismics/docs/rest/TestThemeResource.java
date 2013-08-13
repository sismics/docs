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
 * Test the theme resource.
 * 
 * @author jtremeaux
 */
public class TestThemeResource extends BaseJerseyTest {
    /**
     * Test the theme resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testThemeResource() throws JSONException {
        WebResource themeResource = resource().path("/theme");
        ClientResponse response = themeResource.get(ClientResponse.class);
        response = themeResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        JSONArray theme = json.getJSONArray("themes");
        Assert.assertTrue(theme.length() > 0);
    }
}