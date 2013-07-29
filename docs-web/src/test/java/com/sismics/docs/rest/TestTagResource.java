package com.sismics.docs.rest;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.docs.rest.BaseJerseyTest;
import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Test the tag resource.
 * 
 * @author bgamard
 */
public class TestTagResource extends BaseJerseyTest {
    /**
     * Test the tag resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testTagResource() throws JSONException {
        // Login tag1
        clientUtil.createUser("tag1");
        String tag1Token = clientUtil.login("tag1");
        
        // Create a tag
        WebResource tagResource = resource().path("/tag");
        tagResource.addFilter(new CookieAuthenticationFilter(tag1Token));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("name", "Tag 3");
        ClientResponse response = tagResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String tag3Id = json.optString("id");
        Assert.assertNotNull(tag3Id);
        
        // Get all tags
        tagResource = resource().path("/tag/list");
        tagResource.addFilter(new CookieAuthenticationFilter(tag1Token));
        response = tagResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray tags = json.getJSONArray("tags");
        Assert.assertTrue(tags.length() > 0);
        
        // Deletes a tag
        tagResource = resource().path("/tag/" + tag3Id);
        tagResource.addFilter(new CookieAuthenticationFilter(tag1Token));
        response = tagResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Get all tags
        tagResource = resource().path("/tag/list");
        tagResource.addFilter(new CookieAuthenticationFilter(tag1Token));
        response = tagResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        tags = json.getJSONArray("tags");
        Assert.assertTrue(tags.length() == 0);
    }
}