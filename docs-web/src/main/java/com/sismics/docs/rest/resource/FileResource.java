package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
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
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.NoResultException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String id) throws JSONException {
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
     * @param id Document ID
     * @param order List of files ID in the new order
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
            @PathParam("id") final String id,
            @QueryParam("thumbnail") boolean thumbnail) throws JSONException {
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

        
        // Get the stored file
        java.io.File storedfile = null;
        if (thumbnail) {
            if (ImageUtil.isImage(file.getMimeType())) {
                storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), id + "_thumb").toFile();
            } else {
                storedfile = new java.io.File(getClass().getResource("/image/file.png").getFile());
            }
        } else {
            storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), id).toFile();
        }

        return Response.ok(storedfile)
                .header("Content-Type", file.getMimeType())
                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000 * 24 * 7))
                .build();
    }
}
