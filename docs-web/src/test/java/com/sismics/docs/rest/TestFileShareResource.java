package com.sismics.docs.rest;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

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
 * Exhaustive test of the file share resource.
 * 
 * @author bgamard
 */
public class TestFileShareResource extends BaseJerseyTest {
    /**
     * Test the file share resource.
     * 
     * @throws Exception
     */
    @Test
    public void testFileShareResource() throws Exception {
        // Login fileshare1
        clientUtil.createUser("fileshare1");
        String fileShare1AuthenticationToken = clientUtil.login("fileshare1");
        
        // Create a document
        WebResource documentResource = resource().path("/document");
        documentResource.addFilter(new CookieAuthenticationFilter(fileShare1AuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("title", "File test document 1");
        ClientResponse response = documentResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String document1Id = json.optString("id");
        Assert.assertNotNull(document1Id);
        
        // Add a file
        WebResource fileResource = resource().path("/file");
        fileResource.addFilter(new CookieAuthenticationFilter(fileShare1AuthenticationToken));
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
        
        // Share this file
        WebResource fileShareResource = resource().path("/fileshare");
        fileShareResource.addFilter(new CookieAuthenticationFilter(fileShare1AuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("id", file1Id);
        response = fileShareResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String fileShare1Id = json.getString("id");

        // Get the file data anonymously
        fileResource = resource().path("/file/" + file1Id + "/data");
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        getParams.putSingle("thumbnail", false);
        getParams.putSingle("share", fileShare1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        InputStream is = response.getEntityInputStream();
        byte[] fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(163510, fileBytes.length);
        
        // Get the file
        fileResource = resource().path("/file/" + file1Id);
        fileResource.addFilter(new CookieAuthenticationFilter(fileShare1AuthenticationToken));
        response = fileResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(file1Id, json.getString("id"));
        Assert.assertEquals(1, json.getJSONArray("shares").length());
        Assert.assertEquals(fileShare1Id, json.getJSONArray("shares").getJSONObject(0).getString("id"));
        
        // Deletes the share
        fileShareResource = resource().path("/fileshare/" + fileShare1Id);
        fileShareResource.addFilter(new CookieAuthenticationFilter(fileShare1AuthenticationToken));
        response = fileShareResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

    }
}