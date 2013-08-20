package com.sismics.docs.rest;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sismics.util.mime.MimeType;
import com.sismics.util.mime.MimeTypeUtil;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Exhaustive test of the document resource.
 * 
 * @author bgamard
 */
public class TestDocumentResource extends BaseJerseyTest {
    /**
     * Test the document resource.
     * @throws Exception 
     */
    @Test
    public void testDocumentResource() throws Exception {
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
        String file1Id = json.getString("id");
        
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
        
        // Search documents
        Assert.assertEquals(1, searchDocuments("full:uranium full:einstein", document1Token));
        Assert.assertEquals(1, searchDocuments("full:title", document1Token));
        Assert.assertEquals(1, searchDocuments("title", document1Token));
        Assert.assertEquals(1, searchDocuments("super description", document1Token));
        Assert.assertEquals(1, searchDocuments("at:" + DateTimeFormat.forPattern("yyyy").print(new Date().getTime()), document1Token));
        Assert.assertEquals(1, searchDocuments("at:" + DateTimeFormat.forPattern("yyyy-MM").print(new Date().getTime()), document1Token));
        Assert.assertEquals(1, searchDocuments("at:" + DateTimeFormat.forPattern("yyyy-MM-dd").print(new Date().getTime()), document1Token));
        Assert.assertEquals(1, searchDocuments("after:2010 before:2040-08", document1Token));
        Assert.assertEquals(1, searchDocuments("tag:super", document1Token));
        Assert.assertEquals(1, searchDocuments("shared:yes", document1Token));
        Assert.assertEquals(1, searchDocuments("lang:eng", document1Token));
        Assert.assertEquals(1, searchDocuments("after:2010 before:2040-08 tag:super shared:yes lang:eng title description full:uranium", document1Token));

        // Search documents (nothing)
        Assert.assertEquals(0, searchDocuments("random", document1Token));
        Assert.assertEquals(0, searchDocuments("full:random", document1Token));
        Assert.assertEquals(0, searchDocuments("after:2010 before:2011-05-20", document1Token));
        Assert.assertEquals(0, searchDocuments("at:2040-05-35", document1Token));
        Assert.assertEquals(0, searchDocuments("after:2010-18 before:2040-05-38", document1Token));
        Assert.assertEquals(0, searchDocuments("after:2010-18", document1Token));
        Assert.assertEquals(0, searchDocuments("before:2040-05-38", document1Token));
        Assert.assertEquals(0, searchDocuments("tag:Nop", document1Token));
        Assert.assertEquals(0, searchDocuments("lang:fra", document1Token));

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
        
        // Search documents by query
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "super");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
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
        
        // Check that the associated files are deleted from FS
        java.io.File storedFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file1Id).toFile();
        java.io.File webFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file1Id + "_web").toFile();
        java.io.File thumbnailFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file1Id + "_thumb").toFile();
        Assert.assertFalse(storedFile.exists());
        Assert.assertFalse(webFile.exists());
        Assert.assertFalse(thumbnailFile.exists());
        
        // Get a document (KO)
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(document1Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
    }
    
    /**
     * Search documents and returns the number found.
     * 
     * @param query Search query
     * @param token Authentication token
     * @return Number of documents found
     * @throws Exception
     */
    private int searchDocuments(String query, String token) throws Exception {
        WebResource documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(token));
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", query);
        ClientResponse response = documentResource.queryParams(getParams).get(ClientResponse.class);
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        return json.getJSONArray("documents").length();
    }
    
    /**
     * Test PDF extraction.
     * 
     * @throws Exception 
     */
    @Test
    public void testPdfExtraction() throws Exception {
        // Login document2
        clientUtil.createUser("document2");
        String document2Token = clientUtil.login("document2");

        // Create a document
        WebResource documentResource = resource().path("/document");
        documentResource.addFilter(new CookieAuthenticationFilter(document2Token));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("title", "My super title document 1");
        postParams.add("description", "My super description for document 1");
        postParams.add("language", "eng");
        long create1Date = new Date().getTime();
        postParams.add("create_date", create1Date);
        ClientResponse response = documentResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String document1Id = json.optString("id");
        Assert.assertNotNull(document1Id);
        
        // Add a PDF file
        WebResource fileResource = resource().path("/file");
        fileResource.addFilter(new CookieAuthenticationFilter(document2Token));
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream file = this.getClass().getResourceAsStream("/file/wikipedia.pdf");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(file),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        form.field("id", document1Id);
        response = fileResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String file1Id = json.getString("id");
        
        // Search documents by query in full content
        documentResource = resource().path("/document/list");
        documentResource.addFilter(new CookieAuthenticationFilter(document2Token));
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        getParams.putSingle("search", "full:vrandecic");
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertTrue(json.getJSONArray("documents").length() == 1);
        
        // Get the file thumbnail data
        fileResource = resource().path("/file/" + file1Id + "/data");
        fileResource.addFilter(new CookieAuthenticationFilter(document2Token));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("size", "thumb");
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        InputStream is = response.getEntityInputStream();
        byte[] fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(MimeType.IMAGE_JPEG, MimeTypeUtil.guessMimeType(fileBytes));
        Assert.assertEquals(33691, fileBytes.length);
    }
}