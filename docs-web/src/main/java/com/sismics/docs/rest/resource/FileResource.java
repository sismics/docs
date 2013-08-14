package com.sismics.docs.rest.resource;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.ShareDao;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.FileUtil;
import com.sismics.util.ImageUtil;
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
     * Add a file to a document.
     * 
     * @param documentId Document ID
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
        Document document;
        try {
            document = documentDao.getDocument(documentId, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", documentId));
        }
        
        FileDao fileDao = new FileDao();
        
        // Validate mime type
        InputStream is = new BufferedInputStream(fileBodyPart.getValueAs(InputStream.class));
        String mimeType;
        try {
            mimeType = MimeTypeUtil.guessMimeType(is);
        } catch (Exception e) {
            throw new ServerException("ErrorGuessMime", "Error guessing mime type", e);
        }
        if (mimeType == null) {
            throw new ClientException("InvalidFileType", "File type not recognized");
        }
        
        try {
            // Get files of this document
            int order = 0;
            for (File file : fileDao.getByDocumentId(documentId)) {
                file.setOrder(order++);
            }
            
            // Create the file
            File file = new File();
            file.setOrder(order);
            file.setDocumentId(document.getId());
            file.setMimeType(mimeType);
            String fileId = fileDao.create(file);
            
            // Save the file
            FileUtil.save(is, file);

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
     * Reorder files.
     * 
     * @param documentId Document ID
     * @param idList List of files ID in the new order
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("reorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response reorder(
            @FormParam("id") String documentId,
            @FormParam("order") List<String> idList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(documentId, "id");
        ValidationUtil.validateRequired(idList, "order");
        
        // Get the document
        DocumentDao documentDao = new DocumentDao();
        try {
            documentDao.getDocument(documentId, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", documentId));
        }
        
        // Reorder files
        FileDao fileDao = new FileDao();
        for (File file : fileDao.getByDocumentId(documentId)) {
            int order = idList.lastIndexOf(file.getId());
            if (order != -1) {
                file.setOrder(order);
            }
        }
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns files linked to a document.
     * 
     * @param documentId Document ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("id") String documentId,
            @QueryParam("share") String shareId) throws JSONException {
        authenticate();
        
        // Check document visibility
        DocumentDao documentDao = new DocumentDao();
        Document document = documentDao.getDocument(documentId);
        ShareDao shareDao = new ShareDao();
        if (!shareDao.checkVisibility(document, principal.getId(), shareId)) {
            throw new ForbiddenClientException();
        }
        
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.getByDocumentId(documentId);

        JSONObject response = new JSONObject();
        List<JSONObject> files = new ArrayList<>();
        
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
        DocumentDao documentDao = new DocumentDao();
        File file;
        try {
            file = fileDao.getFile(id);
            documentDao.getDocument(file.getDocumentId(), principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("FileNotFound", MessageFormat.format("File not found: {0}", id));
        }
        
        // Delete the file
        // TODO Delete the file from storage too
        fileDao.delete(file.getId());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns a file.
     * 
     * @param fileId File ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/data")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response data(
            @PathParam("id") final String fileId,
            @QueryParam("share") String shareId,
            @QueryParam("thumbnail") boolean thumbnail) throws JSONException {
        authenticate();
        
        // Get the file
        FileDao fileDao = new FileDao();
        DocumentDao documentDao = new DocumentDao();
        File file;
        try {
            file = fileDao.getFile(fileId);
            Document document = documentDao.getDocument(file.getDocumentId());
            
            // Check document visibility
            ShareDao shareDao = new ShareDao();
            if (!shareDao.checkVisibility(document, principal.getId(), shareId)) {
                throw new ForbiddenClientException();
            }
        } catch (NoResultException e) {
            throw new ClientException("FileNotFound", MessageFormat.format("File not found: {0}", fileId));
        }

        
        // Get the stored file
        java.io.File storedfile;
        if (thumbnail) {
            if (ImageUtil.isImage(file.getMimeType())) {
                storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), fileId + "_thumb").toFile();
            } else {
                storedfile = new java.io.File(getClass().getResource("/image/file.png").getFile());
            }
        } else {
            storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), fileId).toFile();
        }

        return Response.ok(storedfile)
                .header("Content-Type", file.getMimeType())
                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000 * 24 * 7))
                .build();
    }
}
