package com.sismics.docs.rest;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Exhaustive test of the share resource.
 * 
 * @author bgamard
 */
public class TestShareResource extends BaseJerseyTest {
    /**
     * Test the share resource.
     * 
     * @throws Exception
     */
    @Test
    public void testShareResource() throws Exception {
        // Login share1
        clientUtil.createUser("share1");
        String share1AuthenticationToken = clientUtil.login("share1");
        
        // Create a document
        WebResource documentResource = resource().path("/document");
        documentResource.addFilter(new CookieAuthenticationFilter(share1AuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("title", "File test document 1");
        postParams.add("language", "eng");
        ClientResponse response = documentResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String document1Id = json.optString("id");
        Assert.assertNotNull(document1Id);
        
        // Add a file
        WebResource fileResource = resource().path("/file");
        fileResource.addFilter(new CookieAuthenticationFilter(share1AuthenticationToken));
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream file = this.getClass().getResourceAsStream("/file/PIA00452.jpg");
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
        WebResource shareResource = resource().path("/share");
        shareResource.addFilter(new CookieAuthenticationFilter(share1AuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("id", document1Id);
        postParams.add("name", "4 All");
        response = shareResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String share1Id = json.getString("id");
        
        // Get the document anonymously
        documentResource = resource().path("/document/" + document1Id);
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        getParams.putSingle("share", share1Id);
        response = documentResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(document1Id, json.getString("id"));
        Assert.assertEquals(1, json.getJSONArray("shares").length());
        Assert.assertEquals(share1Id, json.getJSONArray("shares").getJSONObject(0).getString("id"));
        Assert.assertEquals("4 All", json.getJSONArray("shares").getJSONObject(0).getString("name"));

        // Get all files from this document anonymously
        fileResource = resource().path("/file/list");
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("id", document1Id);
        getParams.putSingle("share", share1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONArray files = json.getJSONArray("files");
        Assert.assertEquals(1, files.length());
        
        // Get the file data anonymously
        fileResource = resource().path("/file/" + file1Id + "/data");
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("thumbnail", false);
        getParams.putSingle("share", share1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        InputStream is = response.getEntityInputStream();
        byte[] fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(163510, fileBytes.length);
        
        // Deletes the share
        shareResource = resource().path("/share/" + share1Id);
        shareResource.addFilter(new CookieAuthenticationFilter(share1AuthenticationToken));
        response = shareResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

    }
}