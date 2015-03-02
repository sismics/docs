package com.sismics.docs.rest.resource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.ShareDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.mime.MimeType;
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
     * Add a file (with or without a document).
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
        ValidationUtil.validateRequired(fileBodyPart, "file");

        // Get the current user
        UserDao userDao = new UserDao();
        User user = userDao.getById(principal.getId());
        
        // Get the document
        Document document = null;
        if (Strings.isNullOrEmpty(documentId)) {
            documentId = null;
        } else {
            DocumentDao documentDao = new DocumentDao();
            try {
                document = documentDao.getDocument(documentId, principal.getId());
            } catch (NoResultException e) {
                throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", documentId));
            }
        }
        
        // Keep unencrypted data in memory, because we will need it two times
        byte[] fileData;
        try {
            fileData = ByteStreams.toByteArray(fileBodyPart.getValueAs(InputStream.class));
        } catch (IOException e) {
            throw new ServerException("StreamError", "Error reading the input file", e);
        }
        InputStream fileInputStream = new ByteArrayInputStream(fileData);
        
        // Validate mime type
        String mimeType;
        try {
            mimeType = MimeTypeUtil.guessMimeType(fileInputStream);
        } catch (Exception e) {
            throw new ServerException("ErrorGuessMime", "Error guessing mime type", e);
        }
        if (mimeType == null) {
            throw new ClientException("InvalidFileType", "File type not recognized");
        }
        
        try {
            // Get files of this document
            FileDao fileDao = new FileDao();
            int order = 0;
            if (documentId != null) {
                for (File file : fileDao.getByDocumentId(documentId)) {
                    file.setOrder(order++);
                }
            }
            
            // Create the file
            File file = new File();
            file.setOrder(order);
            file.setDocumentId(documentId);
            file.setMimeType(mimeType);
            String fileId = fileDao.create(file);
            
            // Save the file
            FileUtil.save(fileInputStream, file, user.getPrivateKey());
            
            // Raise a new file created event if we have a document
            if (documentId != null) {
                FileCreatedAsyncEvent fileCreatedAsyncEvent = new FileCreatedAsyncEvent();
                fileCreatedAsyncEvent.setDocument(document);
                fileCreatedAsyncEvent.setFile(file);
                fileCreatedAsyncEvent.setInputStream(fileInputStream);
                AppContext.getInstance().getAsyncEventBus().post(fileCreatedAsyncEvent);
            }

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
     * Attach a file to a document.
     * 
     * @param id File ID
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response attach(
            @PathParam("id") String id,
            @FormParam("id") String documentId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        ValidationUtil.validateRequired(documentId, "id");
        
        // Get the current user
        UserDao userDao = new UserDao();
        User user = userDao.getById(principal.getId());
        
        // Get the document and the file
        DocumentDao documentDao = new DocumentDao();
        FileDao fileDao = new FileDao();
        Document document;
        File file;
        try {
            file = fileDao.getFile(id);
            document = documentDao.getDocument(documentId, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", documentId));
        }
        
        // Check that the file is orphan
        if (file.getDocumentId() != null) {
            throw new ClientException("IllegalFile", MessageFormat.format("File not orphan: {0}", id));
        }
        
        // Update the file
        // TODO Reorder files to put the new one at the end
        file.setDocumentId(documentId);
        fileDao.updateDocument(file);
        
        // Raise a new file created event (it wasn't sent during file creation)
        try {
            java.io.File storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), id).toFile();
            InputStream fileInputStream = new FileInputStream(storedfile);
            final InputStream responseInputStream = EncryptionUtil.decryptInputStream(fileInputStream, user.getPrivateKey());
            FileCreatedAsyncEvent fileCreatedAsyncEvent = new FileCreatedAsyncEvent();
            fileCreatedAsyncEvent.setDocument(document);
            fileCreatedAsyncEvent.setFile(file);
            fileCreatedAsyncEvent.setInputStream(responseInputStream);
            AppContext.getInstance().getAsyncEventBus().post(fileCreatedAsyncEvent);
        } catch (Exception e) {
            throw new ClientException("AttachError", "Error attaching file to document", e);
        }
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
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
        try {
            DocumentDao documentDao = new DocumentDao();
            Document document = documentDao.getDocument(documentId);
            ShareDao shareDao = new ShareDao();
            if (!shareDao.checkVisibility(document, principal.getId(), shareId)) {
                throw new ForbiddenClientException();
            }
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", documentId));
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
        fileDao.delete(file.getId());
        
        // Raise a new file deleted event
        FileDeletedAsyncEvent fileDeletedAsyncEvent = new FileDeletedAsyncEvent();
        fileDeletedAsyncEvent.setFile(file);
        AppContext.getInstance().getAsyncEventBus().post(fileDeletedAsyncEvent);
        
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
            @QueryParam("size") String size) throws JSONException {
        authenticate();
        
        if (size != null) {
            if (!Lists.newArrayList("web", "thumb").contains(size)) {
                throw new ClientException("SizeError", "Size must be web or thumb");
            }
        }
        
        // Get the file
        FileDao fileDao = new FileDao();
        DocumentDao documentDao = new DocumentDao();
        UserDao userDao = new UserDao();
        File file;
        Document document;
        try {
            file = fileDao.getFile(fileId);
            document = documentDao.getDocument(file.getDocumentId());
            
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
        String mimeType;
        boolean decrypt = false;
        if (size != null) {
            storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), fileId + "_" + size).toFile();
            mimeType = MimeType.IMAGE_JPEG; // Thumbnails are JPEG
            decrypt = true; // Thumbnails are encrypted
            if (!storedfile.exists()) {
                storedfile = new java.io.File(getClass().getResource("/image/file.png").getFile());
                mimeType = MimeType.IMAGE_PNG;
                decrypt = false;
            }
        } else {
            storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), fileId).toFile();
            mimeType = file.getMimeType();
            decrypt = true; // Original files are encrypted
        }
        
        // Stream the output and decrypt it if necessary
        StreamingOutput stream;
        User user = userDao.getById(document.getUserId());
        try {
            InputStream fileInputStream = new FileInputStream(storedfile);
            final InputStream responseInputStream = decrypt ?
                    EncryptionUtil.decryptInputStream(fileInputStream, user.getPrivateKey()) : fileInputStream;
                    
            stream = new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                    try {
                        ByteStreams.copy(responseInputStream, outputStream);
                    } finally {
                        responseInputStream.close();
                        outputStream.close();
                    }
                }
            };
        } catch (Exception e) {
            throw new ServerException("FileError", "Error while reading the file", e);
        }

        return Response.ok(stream)
                .header("Content-Type", mimeType)
                .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000 * 24))
                .build();
    }
    
    /**
     * Returns all files from a document, zipped.
     * 
     * @param documentId Document ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("zip")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response zip(
            @QueryParam("id") String documentId,
            @QueryParam("share") String shareId) throws JSONException {
        authenticate();
        
        // Get the document
        DocumentDao documentDao = new DocumentDao();
        Document document;
        try {
            document = documentDao.getDocument(documentId);
            
            // Check document visibility
            ShareDao shareDao = new ShareDao();
            if (!shareDao.checkVisibility(document, principal.getId(), shareId)) {
                throw new ForbiddenClientException();
            }
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", documentId));
        }
        
        // Get files and user associated with this document
        FileDao fileDao = new FileDao();
        UserDao userDao = new UserDao();
        final List<File> fileList = fileDao.getByDocumentId(documentId);
        final User user = userDao.getById(document.getUserId());
        
        // Create the ZIP stream
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                    // Add each file to the ZIP stream
                    int index = 0;
                    for (File file : fileList) {
                        java.io.File storedfile = Paths.get(DirectoryUtil.getStorageDirectory().getPath(), file.getId()).toFile();
                        InputStream fileInputStream = new FileInputStream(storedfile);
                        
                        // Add the decrypted file to the ZIP stream
                        try (InputStream decryptedStream = EncryptionUtil.decryptInputStream(fileInputStream, user.getPrivateKey())) {
                            ZipEntry zipEntry = new ZipEntry(index + "." + MimeTypeUtil.getFileExtension(file.getMimeType()));
                            zipOutputStream.putNextEntry(zipEntry);
                            ByteStreams.copy(decryptedStream, zipOutputStream);
                            zipOutputStream.closeEntry();
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new WebApplicationException(e);
                        }
                        index++;
                    }
                }
                outputStream.close();
            }
        };
        
        // Write to the output
        return Response.ok(stream)
                .header("Content-Type", "application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + document.getTitle().replaceAll("\\W+", "_") + ".zip\"")
                .build();
    }
}
