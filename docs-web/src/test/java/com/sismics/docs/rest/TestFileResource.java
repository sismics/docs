package com.sismics.docs.rest;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import com.sismics.util.mime.MimeType;
import com.sismics.util.mime.MimeTypeUtil;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.Assert;
import org.junit.Test;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.zip.ZipInputStream;

/**
 * Exhaustive test of the file resource.
 * 
 * @author bgamard
 */
public class TestFileResource extends BaseJerseyTest {
    /**
     * Test the file resource.
     * 
     * @throws Exception e
     */
    @Test
    public void testFileResource() throws Exception {
        // Login file_resources
        clientUtil.createUser("file_resources");
        String file1Token = clientUtil.login("file_resources");
        
        // Create a document
        String document1Id = clientUtil.createDocument(file1Token);
        
        // Add a file
        String file1Id = clientUtil.addFileToDocument(FILE_PIA_00452_JPG, file1Token, document1Id);
        
        // Add a file
        String file2Id = clientUtil.addFileToDocument(FILE_PIA_00452_JPG, file1Token, document1Id);
        
        // Get the file data
        Response response = target().path("/file/" + file1Id + "/data").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get();
        InputStream is = (InputStream) response.getEntity();
        byte[] fileBytes = ByteStreams.toByteArray(is);
        Assert.assertTrue(fileBytes.length > 0);
        
        // Get the thumbnail data
        response = target().path("/file/" + file1Id + "/data")
                .queryParam("size", "thumb")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        is = (InputStream) response.getEntity();
        fileBytes = ByteStreams.toByteArray(is);
        Assert.assertTrue(fileBytes.length > 0);
        
        // Get the content data
        response = target().path("/file/" + file1Id + "/data")
                .queryParam("size", "content")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Get the web data
        response = target().path("/file/" + file1Id + "/data")
                .queryParam("size", "web")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        is = (InputStream) response.getEntity();
        fileBytes = ByteStreams.toByteArray(is);
        Assert.assertTrue(fileBytes.length > 0);
        
        // Check that the files are not readable directly from FS
        Path storedFile = DirectoryUtil.getStorageDirectory().resolve(file1Id);
        Assert.assertEquals(MimeType.DEFAULT, MimeTypeUtil.guessMimeType(storedFile, null));

        // Get all files from a document
        JsonObject json = target().path("/file/list")
                .queryParam("id", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get(JsonObject.class);
        JsonArray files = json.getJsonArray("files");
        Assert.assertEquals(2, files.size());
        Assert.assertEquals(file1Id, files.getJsonObject(0).getString("id"));
        Assert.assertEquals("PIA00452.jpg", files.getJsonObject(0).getString("name"));
        Assert.assertEquals("image/jpeg", files.getJsonObject(0).getString("mimetype"));
        Assert.assertEquals(0, files.getJsonObject(0).getInt("version"));
        Assert.assertEquals(163510L, files.getJsonObject(0).getJsonNumber("size").longValue());
        Assert.assertEquals(file2Id, files.getJsonObject(1).getString("id"));
        Assert.assertEquals("PIA00452.jpg", files.getJsonObject(1).getString("name"));
        Assert.assertEquals(0, files.getJsonObject(1).getInt("version"));

        // Rename a file
        target().path("file/" + file1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .post(Entity.form(new Form()
                        .param("name", "Pale Blue Dot")), JsonObject.class);

        // Get all files from a document
        json = target().path("/file/list")
                .queryParam("id", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(2, files.size());
        Assert.assertEquals(file1Id, files.getJsonObject(0).getString("id"));
        Assert.assertEquals("Pale Blue Dot", files.getJsonObject(0).getString("name"));

        // Reorder files
        target().path("/file/reorder").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .post(Entity.form(new Form()
                        .param("id", document1Id)
                        .param("order", file2Id)
                        .param("order", file1Id)), JsonObject.class);

        // Get all files from a document
        json = target().path("/file/list")
                .queryParam("id", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(2, files.size());
        Assert.assertEquals(file2Id, files.getJsonObject(0).getString("id"));
        Assert.assertEquals(file1Id, files.getJsonObject(1).getString("id"));
        
        // Get a ZIP from all files
        response = target().path("/file/zip")
                .queryParam("id", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));

        // Deletes a file
        json = target().path("/file/" + file1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Get the file data (not found)
        response = target().path("/file/" + file1Id + "/data").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
        
        // Check that files are deleted from FS
        storedFile = DirectoryUtil.getStorageDirectory().resolve(file1Id);
        Path webFile = DirectoryUtil.getStorageDirectory().resolve(file1Id + "_web");
        Path thumbnailFile = DirectoryUtil.getStorageDirectory().resolve(file1Id + "_thumb");
        Assert.assertFalse(Files.exists(storedFile));
        Assert.assertFalse(Files.exists(webFile));
        Assert.assertFalse(Files.exists(thumbnailFile));
        
        // Get all files from a document
        json = target().path("/file/list")
                .queryParam("id", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(1, files.size());

        // Process a file
        target().path("/file/" + file2Id + "/process").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .post(Entity.form(new Form()), JsonObject.class);

        // Get all versions from a file
        json = target().path("/file/" + file2Id + "/versions")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(1, files.size());
        JsonObject file = files.getJsonObject(0);
        Assert.assertEquals(file2Id, file.getString("id"));
        Assert.assertEquals("PIA00452.jpg", file.getString("name"));
        Assert.assertEquals("image/jpeg", file.getString("mimetype"));
        Assert.assertEquals(0, file.getInt("version"));

        // Add a new version to a file
        String file3Id;
        try (InputStream is0 = Resources.getResource("file/document.txt").openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file", is0, "document.txt");
            try (FormDataMultiPart multiPart = new FormDataMultiPart()) {
                json = target()
                        .register(MultiPartFeature.class)
                        .path("/file").request()
                        .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                        .put(Entity.entity(
                                multiPart
                                        .field("id", document1Id)
                                        .field("previousFileId", file2Id)
                                        .bodyPart(streamDataBodyPart),
                                MediaType.MULTIPART_FORM_DATA_TYPE), JsonObject.class);
                file3Id = json.getString("id");
                Assert.assertNotNull(file2Id);
            }
        }

        // Get all versions from a file
        json = target().path("/file/" + file2Id + "/versions")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(2, files.size());
        file = files.getJsonObject(1);
        Assert.assertEquals(file3Id, file.getString("id"));
        Assert.assertEquals("document.txt", file.getString("name"));
        Assert.assertEquals("text/plain", file.getString("mimetype"));
        Assert.assertEquals(1, file.getInt("version"));

        // Delete the previous version
        json = target().path("/file/" + file2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check the newly created version
        json = target().path("/file/list")
                .queryParam("id", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(1, files.size());
        Assert.assertEquals(file3Id, files.getJsonObject(0).getString("id"));
        Assert.assertEquals("document.txt", files.getJsonObject(0).getString("name"));
        Assert.assertEquals(1, files.getJsonObject(0).getInt("version"));
    }
    
    @Test
    public void testFileResourceZip() throws Exception {
        // Login file_resources
        clientUtil.createUser("file_resources_zip");
        String file1Token = clientUtil.login("file_resources_zip");

        // Create a document
        String document1Id = clientUtil.createDocument(file1Token);

        // Add a file
        String file1Id = clientUtil.addFileToDocument(FILE_PIA_00452_JPG, file1Token, document1Id);

        // Get a ZIP from all files of the document
        Response response = target().path("/file/zip")
                .queryParam("id", document1Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        InputStream is = (InputStream) response.getEntity();
        ZipInputStream zipInputStream = new ZipInputStream(is);
        Assert.assertEquals(zipInputStream.getNextEntry().getName(), "0-PIA00452.jpg");
        Assert.assertNull(zipInputStream.getNextEntry());

        // Fail if we don't have access to the document
        response = target().path("/file/zip")
                .queryParam("id", document1Id)
                .request()
                .get();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));

        // Create a document
        String document2Id = clientUtil.createDocument(file1Token);

        // Add a file
        String file2Id = clientUtil.addFileToDocument(FILE_EINSTEIN_ROOSEVELT_LETTER_PNG, file1Token, document2Id);

        // Get a ZIP from both files
        response = target().path("/file/zip")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, file1Token)
                .post(Entity.form(new Form()
                        .param("files", file1Id)
                        .param("files", file2Id)));
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        is = (InputStream) response.getEntity();
        zipInputStream = new ZipInputStream(is);
        Assert.assertNotNull(zipInputStream.getNextEntry().getName());
        Assert.assertNotNull(zipInputStream.getNextEntry().getName());
        Assert.assertNull(zipInputStream.getNextEntry());
        
        // Fail if we don't have access to the files
        response = target().path("/file/zip")
                .request()
                .post(Entity.form(new Form()
                        .param("files", file1Id)
                        .param("files", file2Id)));
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
    }

    /**
     * Test using a ZIP file.
     *
     * @throws Exception e
     */
    @Test
    public void testZipFileUpload() throws Exception {
        // Login file_zip
        clientUtil.createUser("file_zip");
        String fileZipToken = clientUtil.login("file_zip");

        // Create a document
        String document1Id = clientUtil.createDocument(fileZipToken);

        // Add a file
        clientUtil.addFileToDocument(FILE_WIKIPEDIA_ZIP, fileZipToken, document1Id);
    }

    /**
     * Test orphan files (without linked document).
     * 
     * @throws Exception e
     */
    @Test
    public void testOrphanFile() throws Exception {
        // Login file_orphan
        clientUtil.createUser("file_orphan");
        String fileOrphanToken = clientUtil.login("file_orphan");
        
        // Add a file
        String file1Id = clientUtil.addFileToDocument(FILE_PIA_00452_JPG, fileOrphanToken, null);

        // Get all orphan files
        JsonObject json = target().path("/file/list").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileOrphanToken)
                .get(JsonObject.class);
        JsonArray files = json.getJsonArray("files");
        Assert.assertEquals(1, files.size());

        // Get the thumbnail data
        Response response = target().path("/file/" + file1Id + "/data")
                .queryParam("size", "thumb")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileOrphanToken)
                .get();
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        InputStream is = (InputStream) response.getEntity();
        byte[] fileBytes = ByteStreams.toByteArray(is);
        Assert.assertTrue(fileBytes.length > 0);

        // Get the file data
        response = target().path("/file/" + file1Id + "/data").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileOrphanToken)
                .get();
        is = (InputStream) response.getEntity();
        fileBytes = ByteStreams.toByteArray(is);
        Assert.assertEquals(163510, fileBytes.length);
        
        // Create another document
        String document2Id = clientUtil.createDocument(fileOrphanToken);
        
        // Attach a file to a document
        target().path("/file/" + file1Id + "/attach").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileOrphanToken)
                .post(Entity.form(new Form()
                        .param("id", document2Id)), JsonObject.class);
        
        // Get all files from a document
        json = target().path("/file/list")
                .queryParam("id", document2Id)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileOrphanToken)
                .get(JsonObject.class);
        files = json.getJsonArray("files");
        Assert.assertEquals(1, files.size());
        
        // Add a file
        String file2Id = clientUtil.addFileToDocument(FILE_PIA_00452_JPG, fileOrphanToken, null);
        
        // Deletes a file
        json = target().path("/file/" + file2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileOrphanToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
    }
    
    /**
     * Test user quota.
     * 
     * @throws Exception e
     */
    @Test
    public void testQuota() throws Exception {
        // Login file_quota
        clientUtil.createUser("file_quota");
        String fileQuotaToken = clientUtil.login("file_quota");
        
        // Add a file (292641 bytes large)
        String file1Id = clientUtil.addFileToDocument(FILE_EINSTEIN_ROOSEVELT_LETTER_PNG, fileQuotaToken, null);

        // Check current quota
        JsonObject json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileQuotaToken)
                .get(JsonObject.class);
        Assert.assertEquals(292641L, json.getJsonNumber("storage_current").longValue());
        
        // Add a file (292641 bytes large)
        clientUtil.addFileToDocument(FILE_EINSTEIN_ROOSEVELT_LETTER_PNG, fileQuotaToken, null);
        
        // Check current quota
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileQuotaToken)
                .get(JsonObject.class);
        Assert.assertEquals(585282L, json.getJsonNumber("storage_current").longValue());
        
        // Add a file (292641 bytes large)
        clientUtil.addFileToDocument(FILE_EINSTEIN_ROOSEVELT_LETTER_PNG, fileQuotaToken, null);
        
        // Check current quota
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileQuotaToken)
                .get(JsonObject.class);
        Assert.assertEquals(877923L, json.getJsonNumber("storage_current").longValue());
        
        // Add a file (292641 bytes large)
        try {
            clientUtil.addFileToDocument(FILE_EINSTEIN_ROOSEVELT_LETTER_PNG, fileQuotaToken, null);
            Assert.fail();
        } catch (jakarta.ws.rs.BadRequestException ignored) {
        }
        
        // Deletes a file
        json = target().path("/file/" + file1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileQuotaToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));
        
        // Check current quota
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileQuotaToken)
                .get(JsonObject.class);
        Assert.assertEquals(585282L, json.getJsonNumber("storage_current").longValue());

        // Create a document
        long create1Date = new Date().getTime();
        json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileQuotaToken)
                .put(Entity.form(new Form()
                        .param("title", "File test document 1")
                        .param("language", "eng")
                        .param("create_date", Long.toString(create1Date))), JsonObject.class);
        String document1Id = json.getString("id");
        Assert.assertNotNull(document1Id);

        // Add a file to this document (163510 bytes large)
        clientUtil.addFileToDocument(FILE_PIA_00452_JPG, fileQuotaToken, document1Id);

        // Check current quota
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileQuotaToken)
                .get(JsonObject.class);
        Assert.assertEquals(748792, json.getJsonNumber("storage_current").longValue());

        // Deletes the document
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileQuotaToken)
                .delete(JsonObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // Check current quota
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, fileQuotaToken)
                .get(JsonObject.class);
        Assert.assertEquals(585282L, json.getJsonNumber("storage_current").longValue());
    }
}
