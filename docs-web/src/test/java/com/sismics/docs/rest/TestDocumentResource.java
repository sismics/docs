package com.sismics.docs.rest;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Exhaustive test of the document resource.
 * 
 * @author bgamard
 */
public class TestDocumentResource extends BaseJerseyTest {
    /**
     * Test the document resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testDocumentResource() throws JSONException {
        // Login document1
        clientUtil.createUser("document1");
        String document1Token = clientUtil.login("document1");
        
        // Create a document
        WebResource documentResource = resource().path("/document");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("title", "My super document 1");
        postParams.add("description", "My super description for document 1");
        ClientResponse response = documentResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String document1Id = json.optString("id");
        Assert.assertNotNull(document1Id);
        
        // List all documents
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        getParams.putSingle("sort_column", 3);
        getParams.putSingle("asc", false);
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONArray documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 1);
        Assert.assertEquals(document1Id, documents.getJSONObject(0).getString("id"));
        
        // Get a document
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(document1Id, json.getString("id"));
        
        // Update a document
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("title", "My new super document 1");
        postParams.add("description", "My new super description for document 1");
        response = documentResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        
        // Get a document
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertTrue(json.getString("title").contains("new"));
        Assert.assertTrue(json.getString("description").contains("new"));
        
        // Deletes a document
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        response = documentResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Get a document (KO)
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
    }
}