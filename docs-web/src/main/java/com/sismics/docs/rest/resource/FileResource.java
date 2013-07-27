package com.sismics.docs.rest.resource;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.io.ByteStreams;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.mime.MimeTypeUtil;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

/**
 * File REST resources.
 * 
 * @author bgamard
 */
@Path("/file")
public class FileResource extends BaseResource {
    /**
     * Returns a file.
     * 
     * @param id Document ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @QueryParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        FileDao fileDao = new FileDao();
        File fileDb = null;
        try {
            fileDb = fileDao.getFile(id);
        } catch (NoResultException e) {
            throw new ClientException("FileNotFound", MessageFormat.format("File not found: {0}", id));
        }

        JSONObject file = new JSONObject();
        file.put("id", fileDb.getId());
        file.put("mimetype", fileDb.getMimeType());
        file.put("document_id", fileDb.getDocumentId());
        file.put("create_date", fileDb.getCreateDate().getTime());
        
        return Response.ok().entity(file).build();
    }
    
    /**
     * Add a file to a document.
     * 
     * @param id Document ID
     * @param fileBodyPart File to add
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormDataParam("id") String documentId,
            @FormDataParam("file") FormDataBodyPart fileBodyPart) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(documentId, "id");
        ValidationUtil.validateRequired(fileBodyPart, "file");

        // Get the document
        DocumentDao documentDao = new DocumentDao();
        Document document = null;
        try {
            document = documentDao.getDocument(documentId, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", documentId));
        }
        
        FileDao fileDao = new FileDao();
        
        // Validate mime type
        InputStream is = new BufferedInputStream(fileBodyPart.getValueAs(InputStream.class));
        String mimeType = null;
        try {
            mimeType = MimeTypeUtil.guessMimeType(is);
        } catch (Exception e) {
            throw new ServerException("ErrorGuessMime", "Error guessing mime type", e);
        }
        if (mimeType == null) {
            throw new ClientException("InvalidFileType", "File type not recognized");
        }
        
        try {
            // Create the file
            File file = new File();
            file.setDocumentId(document.getId());
            file.setMimeType(mimeType);
            String fileId = fileDao.create(file);
            
            // Copy the incoming stream content into the storage directory
            Files.copy(is, Paths.get(DirectoryUtil.getStorageDirectory().getPath(), fileId));

            // Always return ok
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            response.put("id", fileId);
            return Response.ok().entity(response).build();
        } catch (Exception e) {
            throw new ServerException("FileError", "Error adding a file", e);
        }
    }
    
    /**
     * Returns files linked to a document.
     * 
     * @param id Document ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("id") String documentId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.getByDocumentId(documentId);

        JSONObject response = new JSONObject();
        List<JSONObject> files = new ArrayList<JSONObject>();
        
        for (File fileDb : fileList) {
            JSONObject file = new JSONObject();
            file.put("id", fileDb.getId());
            file.put("mimetype", fileDb.getMimeType());
            file.put("document_id", fileDb.getDocumentId());
            file.put("create_date", fileDb.getCreateDate().getTime());
            files.add(file);
        }
        
        response.put("files", files);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Deletes a file.
     * 
     * @param id File ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the file
        FileDao fileDao = new FileDao();
        File file = null;
        try {
            file = fileDao.getFile(id);
        } catch (NoResultException e) {
            throw new ClientException("FileNotFound", MessageFormat.format("File not found: {0}", id));
        }
        
        // Delete the document
        fileDao.delete(file.getId());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns a file.
     * 
     * @param id File ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/data")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response data(
            @PathParam("id") final String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the file
        java.io.File storageDirectory = DirectoryUtil.getStorageDirectory();
        java.io.File[] matchingFiles = storageDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(java.io.File dir, String name) {
                return name.startsWith(id);
            }
        });
        final java.io.File storageFile = matchingFiles[0];

        // Stream the file to the response
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException {
                ByteStreams.copy(new FileInputStream(storageFile), os);
            }
        };
        return Response.ok(stream)
                .header("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", storageFile.getName()))
                .build();
    }
}
