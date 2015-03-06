package com.sismics.docs.rest;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
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
 * Exhaustive test of the file resource.
 * 
 * @author bgamard
 */
public class TestFileResource extends BaseJerseyTest {
    /**
     * Test the file resource.
     * 
     * @throws Exception
     */
    @Test
    public void testFileResource() throws Exception {
        // Login file1
        clientUtil.createUser("file1");
        String file1AuthenticationToken = clientUtil.login("file1");
        
        // Create a document
        WebResource documentResource = resource().path("/document");
        documentResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
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
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
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
        
        // Add a file
        fileResource = resource().path("/file");
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        form = new FormDataMultiPart();
        file = this.getClass().getResourceAsStream("/file/PIA00452.jpg");
        fdp = new FormDataBodyPart("file",
                new BufferedInputStream(file),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        form.field("id", document1Id);
        response = fileResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String file2Id = json.getString("id");
        
        // Get the file data
        fileResource = resource().path("/file/" + file1Id + "/data");
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        response = fileResource.get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        InputStream is = response.getEntityInputStream();
        byte[] fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(MimeType.IMAGE_JPEG, MimeTypeUtil.guessMimeType(fileBytes));
        Assert.assertEquals(163510, fileBytes.length);
        
        // Get the thumbnail data
        fileResource = resource().path("/file/" + file1Id + "/data");
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        getParams.putSingle("size", "thumb");
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        is = response.getEntityInputStream();
        fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(MimeType.IMAGE_JPEG, MimeTypeUtil.guessMimeType(fileBytes));
        Assert.assertEquals(41935, fileBytes.length);
        
        // Get the web data
        fileResource = resource().path("/file/" + file1Id + "/data");
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("size", "web");
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        is = response.getEntityInputStream();
        fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(MimeType.IMAGE_JPEG, MimeTypeUtil.guessMimeType(fileBytes));
        Assert.assertEquals(551084, fileBytes.length);
        
        // Check that the files are not readable directly from FS
        java.io.File storedFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file1Id).toFile();
        InputStream storedFileInputStream = new BufferedInputStream(new FileInputStream(storedFile));
        Assert.assertNull(MimeTypeUtil.guessMimeType(storedFileInputStream));
        storedFileInputStream.close();
        
        // Get all files from a document
        fileResource = resource().path("/file/list");
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("id", document1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONArray files = json.getJSONArray("files");
        Assert.assertEquals(2, files.length());
        Assert.assertEquals(file1Id, files.getJSONObject(0).getString("id"));
        Assert.assertEquals(file2Id, files.getJSONObject(1).getString("id"));
        
        // Reorder files
        fileResource = resource().path("/file/reorder");
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("id", document1Id);
        postParams.add("order", file2Id);
        postParams.add("order", file1Id);
        response = fileResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Get all files from a document
        fileResource = resource().path("/file/list");
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("id", document1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        files = json.getJSONArray("files");
        Assert.assertEquals(2, files.length());
        Assert.assertEquals(file2Id, files.getJSONObject(0).getString("id"));
        Assert.assertEquals(file1Id, files.getJSONObject(1).getString("id"));
        
        // Get a ZIP from all files
        fileResource = resource().path("/file/zip");
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("id", document1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        is = response.getEntityInputStream();
        fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(MimeType.APPLICATION_ZIP, MimeTypeUtil.guessMimeType(fileBytes));
        
        // Deletes a file
        fileResource = resource().path("/file/" + file1Id);
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        response = fileResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Check that files are deleted from FS
        storedFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file1Id).toFile();
        java.io.File webFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file1Id + "_web").toFile();
        java.io.File thumbnailFile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file1Id + "_thumb").toFile();
        Assert.assertFalse(storedFile.exists());
        Assert.assertFalse(webFile.exists());
        Assert.assertFalse(thumbnailFile.exists());
        
        // Get all files from a document
        fileResource = resource().path("/file/list");
        fileResource.addFilter(new CookieAuthenticationFilter(file1AuthenticationToken));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("id", document1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        files = json.getJSONArray("files");
        Assert.assertEquals(1, files.length());
    }
    
    @Test
    public void testOrphanFile() throws Exception {
        // Login file1
        clientUtil.createUser("file2");
        String file2AuthenticationToken = clientUtil.login("file2");
        
        // Add a file
        WebResource fileResource = resource().path("/file");
        fileResource.addFilter(new CookieAuthenticationFilter(file2AuthenticationToken));
        FormDataMultiPart form = new FormDataMultiPart();
        InputStream file = this.getClass().getResourceAsStream("/file/PIA00452.jpg");
        FormDataBodyPart fdp = new FormDataBodyPart("file",
                new BufferedInputStream(file),
                MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        ClientResponse response = fileResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String file1Id = json.getString("id");
        
        // Get all orphan files
        fileResource = resource().path("/file/list");
        fileResource.addFilter(new CookieAuthenticationFilter(file2AuthenticationToken));
        MultivaluedMapImpl getParams = new MultivaluedMapImpl();
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONArray files = json.getJSONArray("files");
        Assert.assertEquals(1, files.length());
        
        // Create a document
        WebResource documentResource = resource().path("/document");
        documentResource.addFilter(new CookieAuthenticationFilter(file2AuthenticationToken));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("title", "File test document 1");
        postParams.add("language", "eng");
        response = documentResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String document1Id = json.optString("id");
        Assert.assertNotNull(document1Id);
        
        // Attach a file to a document
        documentResource = resource().path("/file/" + file1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(file2AuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("id", document1Id);
        response = documentResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        
        // Get all files from a document
        fileResource = resource().path("/file/list");
        fileResource.addFilter(new CookieAuthenticationFilter(file2AuthenticationToken));
        getParams = new MultivaluedMapImpl();
        getParams.putSingle("id", document1Id);
        response = fileResource.queryParams(getParams).get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        files = json.getJSONArray("files");
        Assert.assertEquals(1, files.length());
    }
}