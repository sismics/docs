package com.sismics.docs.rest;


import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

import junit.framework.Assert;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;

import javax.ws.rs.core.MediaType;

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
        
        // Create a tag
        WebResource tagResource = resource().path("/tag");
        tagResource.addFilter(new CookieAuthenticationFilter(document1Token));
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
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("title", "My super document 1");
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
        
        // Add a file
        WebResource fileResource = resource().path("/file");
        fileResource.addFilter(new CookieAuthenticationFilter(document1Token));
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream file = this.getClass().getResourceAsStream("/file/Einstein-Roosevelt-letter.png");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(file),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        form.field("id", document1Id);
        response = fileResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        
        // Share this document
        WebResource fileShareResource = resource().path("/share");
        fileShareResource.addFilter(new CookieAuthenticationFilter(document1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("id", document1Id);
        response = fileShareResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        
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
        JSONArray tags = documents.getJSONObject(0).getJSONArray("tags");
        Assert.assertTrue(documents.length() == 1);
        Assert.assertEquals(document1Id, documents.getJSONObject(0).getString("id"));
        Assert.assertEquals("eng", documents.getJSONObject(0).getString("language"));
        Assert.assertEquals(1, tags.length());
        Assert.assertEquals(tag1Id, tags.getJSONObject(0).getString("id"));
        Assert.assertEquals("SuperTag", tags.getJSONObject(0).getString("name"));
        Assert.assertEquals("#ffff00", tags.getJSONObject(0).getString("color"));
        
        // Search documents by query
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "uranium");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 1);
        Assert.assertEquals(document1Id, documents.getJSONObject(0).getString("id"));
        Assert.assertEquals(create1Date, documents.getJSONObject(0).getLong("create_date"));
        
        // Search documents by date
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "after:2010 before:2040-08");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 1);
        Assert.assertEquals(document1Id, documents.getJSONObject(0).getString("id"));
        
        // Search documents by tag
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "tag:super");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 1);
        Assert.assertEquals(document1Id, documents.getJSONObject(0).getString("id"));
        
        // Search documents by shared status
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "shared:yes");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 1);
        Assert.assertEquals(document1Id, documents.getJSONObject(0).getString("id"));
        Assert.assertEquals(true, documents.getJSONObject(0).getBoolean("shared"));
        
        // Search documents by language
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "lang:eng");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 1);
        Assert.assertEquals(document1Id, documents.getJSONObject(0).getString("id"));
        Assert.assertEquals("eng", documents.getJSONObject(0).getString("language"));
        
        // Search documents with multiple criteria
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "after:2010 before:2040-08 tag:super shared:yes lang:eng uranium");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 1);
        Assert.assertEquals(document1Id, documents.getJSONObject(0).getString("id"));
        
        // Search documents (nothing)
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "random");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 0);
        
        // Search documents (nothing)
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "after:2010 before:2011-05-20");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 0);
        
        // Search documents (nothing)
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "tag:Nop");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 0);
        
        // Search documents (nothing)
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "lang:fra");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        documents = json.getJSONArray("documents");
        Assert.assertTrue(documents.length() == 0);
        
        // Get a document
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(document1Id, json.getString("id"));
        tags = json.getJSONArray("tags");
        Assert.assertEquals(1, tags.length());
        Assert.assertEquals(tag1Id, tags.getJSONObject(0).getString("id"));
        
        // Create a tag
        tagResource = resource().path("/tag");
        tagResource.addFilter(new CookieAuthenticationFilter(document1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("name", "SuperTag2");
        postParams.add("color", "#00ffff");
        response = tagResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String tag2Id = json.optString("id");
        Assert.assertNotNull(tag1Id);
        
        // Update a document
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("title", "My new super document 1");
        postParams.add("description", "My new super description for document 1");
        postParams.add("tags", tag2Id);
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
        tags = json.getJSONArray("tags");
        Assert.assertEquals(1, tags.length());
        Assert.assertEquals(tag2Id, tags.getJSONObject(0).getString("id"));
        
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