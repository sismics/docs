package com.sismics.docs.rest;

import java.io.InputStream;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.sismics.util.filter.TokenBasedSecurityFilter;

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
        String share1Token = clientUtil.login("share1");
        
        // Create a document
        JsonObject json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, share1Token)
                .put(Entity.form(new Form()
                        .param("title", "File test document 1")
                        .param("language", "eng")), JsonObject.class);
        String document1Id = json.getString("id");
        Assert.assertNotNull(document1Id);
        
        // Add a file
        String file1Id;
        try (InputStream is = Resources.getResource("file/PIA00452.jpg").openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file", is, "PIA00452.jpg");
            try (FormDataMultiPart multiPart = new FormDataMultiPart()) {
                json = target()
                        .register(MultiPartFeature.class)
                        .path("/file").request()
                        .cookie(TokenBasedSecurityFilter.COOKIE_NAME, share1Token)
                        .put(Entity.entity(multiPart.field("id", document1Id).bodyPart(streamDataBodyPart),
                                MediaType.MULTIPART_FORM_DATA_TYPE), JsonObject.class);
                file1Id = json.getString("id");
            }
        }
        
        // Share this document
        json = target().path("/share").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, share1Token)
                .put(Entity.form(new Form()
                        .param("id", document1Id)
                        .param("name", "4 All")), JsonObject.class);
        String share1Id = json.getString("id");
        
        // Get the document anonymously
        json = target().path("/document/" + document1Id)
                .queryParam("share", share1Id)
                .request()
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        Assert.assertEquals(3, json.getJsonArray("acls").size()); // 2 for the creator, 1 for the share

        // Get all comments from this document anonymously
        json = target().path("/comment/" + document1Id)
                .queryParam("share", share1Id)
                .request()
                .get(JsonObject.class);
        JsonArray comments = json.getJsonArray("comments");
        Assert.assertEquals(0, comments.size());
        
        // Get all files from this document anonymously
        json = target().path("/file/list")
                .queryParam("id", document1Id)
                .queryParam("share", share1Id)
                .request()
                .get(JsonObject.class);
        JsonArray files = json.getJsonArray("files");
        Assert.assertEquals(1, files.size());
        
        // Get the file data anonymously
        Response response = target().path("/file/" + file1Id + "/data")
                .queryParam("thumbnail", false)
                .queryParam("share", share1Id)
                .request()
                .get();
        InputStream is = (InputStream) response.getEntity();
        byte[] fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(163510, fileBytes.length);
        
        // Deletes the share (not allowed)
        clientUtil.createUser("share2");
        String share2Token = clientUtil.login("share2");
        response = target().path("/share/" + share1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, share2Token)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("DocumentNotFound", json.getString("type"));
        
        // Deletes the share
        json = target().path("/share/" + share1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, share1Token)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Deletes the share again
        response = target().path("/share/" + share1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, share1Token)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ShareNotFound", json.getString("type"));
    }
}