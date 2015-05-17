package com.sismics.docs.rest;

import java.util.Date;

import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

/**
 * Test the audit log resource.
 * 
 * @author bgamard
 */
public class TestAuditLogResource extends BaseJerseyTest {
    /**
     * Test the audit log resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testAuditLogResource() throws JSONException {
        // Login auditlog1
        clientUtil.createUser("auditlog1");
        String auditlog1Token = clientUtil.login("auditlog1");
        
        // Create a tag
        WebResource tagResource = resource().path("/tag");
        tagResource.addFilter(new CookieAuthenticationFilter(auditlog1Token));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("name", "SuperTag");
        postParams.add("color", "#ffff00");
        ClientResponse response = tagResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String tag1Id = json.optString("id");
        Assert.assertNotNull(tag1Id);

        // Create a document
        WebResource documentResource = resource().path("/document");
        documentResource.addFilter(new CookieAuthenticationFilter(auditlog1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("title", "My super title document 1");
        postParams.add("description", "My super description for document 1");
        postParams.add("tags", tag1Id);
        postParams.add("language", "eng");
        long create1Date = new Date().getTime();
        postParams.add("create_date", create1Date);
        response = documentResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String document1Id = json.optString("id");
        Assert.assertNotNull(document1Id);
        
        // Get all logs for the document
        WebResource auditLogResource = resource().path("/auditlog");
        auditLogResource.addFilter(new CookieAuthenticationFilter(auditlog1Token));
        response = auditLogResource.queryParam("document", document1Id).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        JSONArray logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() == 3);
        
        // Get all logs for the current user
        auditLogResource = resource().path("/auditlog");
        auditLogResource.addFilter(new CookieAuthenticationFilter(auditlog1Token));
        response = auditLogResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() == 3);
        
        // Deletes a tag
        tagResource = resource().path("/tag/" + tag1Id);
        tagResource.addFilter(new CookieAuthenticationFilter(auditlog1Token));
        response = tagResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Get all logs for the current user
        auditLogResource = resource().path("/auditlog");
        auditLogResource.addFilter(new CookieAuthenticationFilter(auditlog1Token));
        response = auditLogResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        logs = json.getJSONArray("logs");
        Assert.assertTrue(logs.length() == 4);
    }
}