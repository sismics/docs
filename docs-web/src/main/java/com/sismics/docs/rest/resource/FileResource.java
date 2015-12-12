package com.sismics.docs.rest.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
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
import com.sismics.rest.util.JsonUtil;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.mime.MimeType;
import com.sismics.util.mime.MimeTypeUtil;

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
     */
    @PUT
    @Consumes("multipart/form-data")
    public Response add(
            @FormDataParam("id") String documentId,
            @FormDataParam("file") FormDataBodyPart fileBodyPart) {
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
            document = documentDao.getDocument(documentId, PermType.WRITE, principal.getId());
            if (document == null) {
                return Response.status(Status.NOT_FOUND).build();
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
        
        // Validate quota
        if (user.getStorageCurrent() + fileData.length > user.getStorageQuota()) {
            throw new ClientException("QuotaReached", "Quota limit reached");
        }
        
        try {
            // Get files of this document
            FileDao fileDao = new FileDao();
            int order = 0;
            if (documentId != null) {
                for (File file : fileDao.getByDocumentId(principal.getId(), documentId)) {
                    file.setOrder(order++);
                }
            }
            
            // Create the file
            File file = new File();
            file.setOrder(order);
            file.setDocumentId(documentId);
            file.setMimeType(mimeType);
            file.setUserId(principal.getId());
            String fileId = fileDao.create(file);
            
            // Guess the mime type a second time, for open document format (first detected as simple ZIP file)
            file.setMimeType(MimeTypeUtil.guessOpenDocumentFormat(file, fileInputStream));
            
            // Convert to PDF if necessary (for thumbnail and text extraction)
            InputStream pdfIntputStream = FileUtil.convertToPdf(fileInputStream, file);
            
            // Save the file
            FileUtil.save(fileInputStream, pdfIntputStream, file, user.getPrivateKey());
            
            // Update the user quota
            user.setStorageCurrent(user.getStorageCurrent() + fileData.length);
            userDao.updateQuota(user);
            
            // Raise a new file created event if we have a document
            if (documentId != null) {
                FileCreatedAsyncEvent fileCreatedAsyncEvent = new FileCreatedAsyncEvent();
                fileCreatedAsyncEvent.setDocument(document);
                fileCreatedAsyncEvent.setFile(file);
                fileCreatedAsyncEvent.setInputStream(fileInputStream);
                fileCreatedAsyncEvent.setPdfInputStream(pdfIntputStream);
                AppContext.getInstance().getAsyncEventBus().post(fileCreatedAsyncEvent);
            }

            // Always return OK
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("id", fileId)
                    .add("size", fileData.length);
            return Response.ok().entity(response.build()).build();
        } catch (Exception e) {
            throw new ServerException("FileError", "Error adding a file", e);
        }
    }
    
    /**
     * Attach a file to a document.
     * 
     * @param id File ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response attach(
            @PathParam("id") String id,
            @FormParam("id") String documentId) {
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
        File file = fileDao.getFile(id, principal.getId());
        Document document = documentDao.getDocument(documentId, PermType.WRITE, principal.getId());
        if (file == null || document == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Check that the file is orphan
        if (file.getDocumentId() != null) {
            throw new ClientException("IllegalFile", MessageFormat.format("File not orphan: {0}", id));
        }
        
        // Update the file
        file.setDocumentId(documentId);
        file.setOrder(fileDao.getByDocumentId(principal.getId(), documentId).size());
        fileDao.update(file);
        
        // Raise a new file created event (it wasn't sent during file creation)
        try {
            java.nio.file.Path storedFile = DirectoryUtil.getStorageDirectory().resolve(id);
            InputStream fileInputStream = Files.newInputStream(storedFile);
            final InputStream responseInputStream = EncryptionUtil.decryptInputStream(fileInputStream, user.getPrivateKey());
            FileCreatedAsyncEvent fileCreatedAsyncEvent = new FileCreatedAsyncEvent();
            fileCreatedAsyncEvent.setDocument(document);
            fileCreatedAsyncEvent.setFile(file);
            fileCreatedAsyncEvent.setInputStream(responseInputStream);
            AppContext.getInstance().getAsyncEventBus().post(fileCreatedAsyncEvent);
        } catch (Exception e) {
            throw new ClientException("AttachError", "Error attaching file to document", e);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Reorder files.
     * 
     * @param documentId Document ID
     * @param idList List of files ID in the new order
     * @return Response
     */
    @POST
    @Path("reorder")
    public Response reorder(
            @FormParam("id") String documentId,
            @FormParam("order") List<String> idList) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(documentId, "id");
        ValidationUtil.validateRequired(idList, "order");
        
        // Get the document
        DocumentDao documentDao = new DocumentDao();
        if (documentDao.getDocument(documentId, PermType.WRITE, principal.getId()) == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Reorder files
        FileDao fileDao = new FileDao();
        for (File file : fileDao.getByDocumentId(principal.getId(), documentId)) {
            int order = idList.lastIndexOf(file.getId());
            if (order != -1) {
                file.setOrder(order);
            }
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns files linked to a document or not linked to any document.
     * 
     * @param documentId Document ID
     * @param shareId Sharing ID
     * @return Response
     */
    @GET
    @Path("list")
    public Response list(
            @QueryParam("id") String documentId,
            @QueryParam("share") String shareId) {
        boolean authenticated = authenticate();
        
        // Check document visibility
        if (documentId != null) {
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(documentId, PermType.READ, shareId == null ? principal.getId() : shareId)) {
                return Response.status(Status.NOT_FOUND).build();
            }
        } else if (!authenticated) {
            throw new ForbiddenClientException();
        }
        
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.getByDocumentId(principal.getId(), documentId);

        JsonArrayBuilder files = Json.createArrayBuilder();
        for (File fileDb : fileList) {
            try {
                files.add(Json.createObjectBuilder()
                        .add("id", fileDb.getId())
                        .add("mimetype", fileDb.getMimeType())
                        .add("document_id", JsonUtil.nullable(fileDb.getDocumentId()))
                        .add("create_date", fileDb.getCreateDate().getTime())
                        .add("size", Files.size(DirectoryUtil.getStorageDirectory().resolve(fileDb.getId()))));
            } catch (IOException e) {
                throw new ServerException("FileError", "Unable to get the size of " + fileDb.getId(), e);
            }
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("files", files);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Deletes a file.
     * 
     * @param id File ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(
            @PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the file
        FileDao fileDao = new FileDao();
        DocumentDao documentDao = new DocumentDao();
        File file = fileDao.getFile(id);
        if (file == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        if (file.getDocumentId() == null) {
            // It's an orphan file
            if (!file.getUserId().equals(principal.getId())) {
                // But not ours
                throw new ForbiddenClientException();
            }
        } else if (documentDao.getDocument(file.getDocumentId(), PermType.WRITE, principal.getId()) == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Delete the file
        fileDao.delete(file.getId());
        
        // Update the user quota
        UserDao userDao = new UserDao();
        User user = userDao.getById(principal.getId());
        java.nio.file.Path storedFile = DirectoryUtil.getStorageDirectory().resolve(id);
        try {
            user.setStorageCurrent(user.getStorageCurrent() - Files.size(storedFile));
            userDao.updateQuota(user);
        } catch (IOException e) {
            // The file doesn't exists on disk, which is weird, but not fatal
        }
        
        // Raise a new file deleted event
        FileDeletedAsyncEvent fileDeletedAsyncEvent = new FileDeletedAsyncEvent();
        fileDeletedAsyncEvent.setFile(file);
        AppContext.getInstance().getAsyncEventBus().post(fileDeletedAsyncEvent);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns a file.
     * 
     * @param fileId File ID
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/data")
    public Response data(
            @PathParam("id") final String fileId,
            @QueryParam("share") String shareId,
            @QueryParam("size") String size) {
        authenticate();
        
        if (size != null) {
            if (!Lists.newArrayList("web", "thumb").contains(size)) {
                throw new ClientException("SizeError", "Size must be web or thumb");
            }
        }
        
        // Get the file
        FileDao fileDao = new FileDao();
        UserDao userDao = new UserDao();
        File file = fileDao.getFile(fileId);
        if (file == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        if (file.getDocumentId() == null) {
            // It's an orphan file
            if (!file.getUserId().equals(principal.getId())) {
                // But not ours
                throw new ForbiddenClientException();
            }
        } else {
            // Check document accessibility
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(file.getDocumentId(), PermType.READ, shareId == null ? principal.getId() : shareId)) {
                throw new ForbiddenClientException();
            }
        }

        
        // Get the stored file
        java.nio.file.Path storedFile;
        String mimeType;
        boolean decrypt = false;
        if (size != null) {
            storedFile = DirectoryUtil.getStorageDirectory().resolve(fileId + "_" + size);
            mimeType = MimeType.IMAGE_JPEG; // Thumbnails are JPEG
            decrypt = true; // Thumbnails are encrypted
            if (!Files.exists(storedFile)) {
                storedFile = Paths.get(getClass().getResource("/image/file.png").getFile());
                mimeType = MimeType.IMAGE_PNG;
                decrypt = false;
            }
        } else {
            storedFile = DirectoryUtil.getStorageDirectory().resolve(fileId);
            mimeType = file.getMimeType();
            decrypt = true; // Original files are encrypted
        }
        
        // Stream the output and decrypt it if necessary
        StreamingOutput stream;
        
        // A file is always encrypted by the creator of it
        User user = userDao.getById(file.getUserId());
        
        // Write the decrypted file to the output
        try {
            InputStream fileInputStream = Files.newInputStream(storedFile);
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
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
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
     */
    @GET
    @Path("zip")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response zip(
            @QueryParam("id") String documentId,
            @QueryParam("share") String shareId) {
        authenticate();
        
        // Get the document
        DocumentDao documentDao = new DocumentDao();
        DocumentDto documentDto = documentDao.getDocument(documentId);
        if (documentDto == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Check document visibility
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(documentId, PermType.READ, shareId == null ? principal.getId() : shareId)) {
            throw new ForbiddenClientException();
        }
        
        // Get files and user associated with this document
        FileDao fileDao = new FileDao();
        final UserDao userDao = new UserDao();
        final List<File> fileList = fileDao.getByDocumentId(principal.getId(), documentId);
        
        // Create the ZIP stream
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                    // Add each file to the ZIP stream
                    int index = 0;
                    for (File file : fileList) {
                        java.nio.file.Path storedfile = DirectoryUtil.getStorageDirectory().resolve(file.getId());
                        InputStream fileInputStream = Files.newInputStream(storedfile);
                        
                        // Add the decrypted file to the ZIP stream
                        // Files are encrypted by the creator of them
                        User user = userDao.getById(file.getUserId());
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
                .header("Content-Disposition", "attachment; filename=\"" + documentDto.getTitle().replaceAll("\\W+", "_") + ".zip\"")
                .build();
    }
}
