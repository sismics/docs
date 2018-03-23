package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;
import com.sismics.docs.core.event.FileCreatedAsyncEvent;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.HttpUtil;
import com.sismics.util.JsonUtil;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.mime.MimeType;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
     * @api {put} /file Add a file
     * @apiDescription A file can be added without associated document, and will go in a temporary storage waiting for one.
     * This resource accepts only multipart/form-data.
     * @apiName PutFile
     * @apiGroup File
     * @apiParam {String} id Document ID
     * @apiParam {String} file File data
     * @apiSuccess {String} status Status OK
     * @apiSuccess {String} id File ID
     * @apiSuccess {Number} size File size (in bytes)
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Document not found
     * @apiError (server) StreamError Error reading the input file
     * @apiError (server) ErrorGuessMime Error guessing mime type
     * @apiError (client) QuotaReached Quota limit reached
     * @apiError (server) FileError Error adding a file
     * @apiPermission user
     * @apiVersion 1.5.0
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

        // Get the document
        DocumentDto documentDto = null;
        if (Strings.isNullOrEmpty(documentId)) {
            documentId = null;
        } else {
            DocumentDao documentDao = new DocumentDao();
            documentDto = documentDao.getDocument(documentId, PermType.WRITE, getTargetIdList(null));
            if (documentDto == null) {
                throw new NotFoundException();
            }
        }
        
        // Keep unencrypted data temporary on disk
        java.nio.file.Path unencryptedFile;
        long fileSize;
        try {
            unencryptedFile = ThreadLocalContext.get().createTemporaryFile();
            Files.copy(fileBodyPart.getValueAs(InputStream.class), unencryptedFile, StandardCopyOption.REPLACE_EXISTING);
            fileSize = Files.size(unencryptedFile);
        } catch (IOException e) {
            throw new ServerException("StreamError", "Error reading the input file", e);
        }

        try {
            String name = fileBodyPart.getContentDisposition() != null ?
                    URLDecoder.decode(fileBodyPart.getContentDisposition().getFileName(), "UTF-8") : null;
            String fileId = FileUtil.createFile(name, unencryptedFile, fileSize, documentDto == null ?
                    null : documentDto.getLanguage(), principal.getId(), documentId);

            // Always return OK
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("status", "ok")
                    .add("id", fileId)
                    .add("size", fileSize);
            return Response.ok().entity(response.build()).build();
        } catch (IOException e) {
            throw new ClientException(e.getMessage(), e.getMessage(), e);
        } catch (Exception e) {
            throw new ServerException("FileError", "Error adding a file", e);
        }
    }
    
    /**
     * Attach a file to a document.
     *
     * @api {post} /file/:fileId/attach Attach a file to a document
     * @apiName PostFileAttach
     * @apiGroup File
     * @apiParam {String} fileId File ID
     * @apiParam {String} id Document ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) IllegalFile File not orphan
     * @apiError (server) AttachError Error attaching file to document
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param id File ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/attach")
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
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.WRITE, getTargetIdList(null));
        if (file == null || documentDto == null) {
            throw new NotFoundException();
        }
        
        // Check that the file is orphan
        if (file.getDocumentId() != null) {
            throw new ClientException("IllegalFile", MessageFormat.format("File not orphan: {0}", id));
        }
        
        // Update the file
        file.setDocumentId(documentId);
        file.setOrder(fileDao.getByDocumentId(principal.getId(), documentId).size());
        fileDao.update(file);
        
        // Raise a new file created event and document updated event (it wasn't sent during file creation)
        try {
            java.nio.file.Path storedFile = DirectoryUtil.getStorageDirectory().resolve(id);
            java.nio.file.Path unencryptedFile = EncryptionUtil.decryptFile(storedFile, user.getPrivateKey());
            FileUtil.startProcessingFile(id);
            FileCreatedAsyncEvent fileCreatedAsyncEvent = new FileCreatedAsyncEvent();
            fileCreatedAsyncEvent.setUserId(principal.getId());
            fileCreatedAsyncEvent.setLanguage(documentDto.getLanguage());
            fileCreatedAsyncEvent.setFile(file);
            fileCreatedAsyncEvent.setUnencryptedFile(unencryptedFile);
            ThreadLocalContext.get().addAsyncEvent(fileCreatedAsyncEvent);
            
            DocumentUpdatedAsyncEvent documentUpdatedAsyncEvent = new DocumentUpdatedAsyncEvent();
            documentUpdatedAsyncEvent.setUserId(principal.getId());
            documentUpdatedAsyncEvent.setDocumentId(documentId);
            ThreadLocalContext.get().addAsyncEvent(documentUpdatedAsyncEvent);
        } catch (Exception e) {
            throw new ServerException("AttachError", "Error attaching file to document", e);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Update a file.
     *
     * @api {post} /file/:id Update a file
     * @apiName PostFile
     * @apiGroup File
     * @apiParam {String} id File ID
     * @apiParam {String} name Name
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission user
     * @apiVersion 1.6.0
     *
     * @param id File ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response update(@PathParam("id") String id,
                           @FormParam("name") String name) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the file
        File file = findFile(id, null);

        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 200, false);

        // Update the file
        FileDao fileDao = new FileDao();
        file.setName(name);
        fileDao.update(file);

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Reorder files.
     *
     * @api {post} /file/:reorder Reorder files
     * @apiName PostFileReorder
     * @apiGroup File
     * @apiParam {String} id Document ID
     * @apiParam {String[]} order List of files ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Document not found
     * @apiPermission user
     * @apiVersion 1.5.0
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
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(documentId, PermType.WRITE, getTargetIdList(null))) {
            throw new NotFoundException();
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
     * @api {post} /file/list Get files
     * @apiName GetFileList
     * @apiGroup File
     * @apiParam {String} id Document ID
     * @apiParam {String} share Share ID
     * @apiSuccess {Object[]} files List of files
     * @apiSuccess {String} files.id ID
     * @apiSuccess {String} files.mimetype MIME type
     * @apiSuccess {String} files.name File name
     * @apiSuccess {String} files.processing True if the file is currently processing
     * @apiSuccess {String} files.document_id Document ID
     * @apiSuccess {String} files.create_date Create date (timestamp)
     * @apiSuccess {String} files.size File size (in bytes)
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Document not found
     * @apiError (server) FileError Unable to get the size of a file
     * @apiPermission none
     * @apiVersion 1.5.0
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
            if (!aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(shareId))) {
                throw new NotFoundException();
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
                        .add("processing", FileUtil.isProcessingFile(fileDb.getId()))
                        .add("name", JsonUtil.nullable(fileDb.getName()))
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
     * @api {delete} /file/:id Delete a file
     * @apiName DeleteFile
     * @apiGroup File
     * @apiParam {String} id File ID
     * @apiParam {String} share Share ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound File or document not found
     * @apiPermission user
     * @apiVersion 1.5.0
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
        File file = findFile(id, null);

        // Delete the file
        FileDao fileDao = new FileDao();
        fileDao.delete(file.getId(), principal.getId());
        
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
        fileDeletedAsyncEvent.setUserId(principal.getId());
        fileDeletedAsyncEvent.setFile(file);
        ThreadLocalContext.get().addAsyncEvent(fileDeletedAsyncEvent);
        
        if (file.getDocumentId() != null) {
            // Raise a new document updated
            DocumentUpdatedAsyncEvent documentUpdatedAsyncEvent = new DocumentUpdatedAsyncEvent();
            documentUpdatedAsyncEvent.setUserId(principal.getId());
            documentUpdatedAsyncEvent.setDocumentId(file.getDocumentId());
            ThreadLocalContext.get().addAsyncEvent(documentUpdatedAsyncEvent);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns a file.
     *
     * @api {delete} /file/:id/data Get a file data
     * @apiName GetFile
     * @apiGroup File
     * @apiParam {String} id File ID
     * @apiParam {String} share Share ID
     * @apiParam {String="web","thumb","content"} [size] Size variation
     * @apiSuccess {Object} file The file data is the whole response
     * @apiError (client) SizeError Size must be web or thumb
     * @apiError (client) ForbiddenError Access denied or document not visible
     * @apiError (client) NotFound File not found
     * @apiError (server) ServiceUnavailable Error reading the file
     * @apiPermission none
     * @apiVersion 1.5.0
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
        
        if (size != null && !Lists.newArrayList("web", "thumb", "content").contains(size)) {
            throw new ClientException("SizeError", "Size must be web, thumb or content");
        }

        // Get the file
        File file = findFile(fileId, shareId);

        // Get the stored file
        UserDao userDao = new UserDao();
        java.nio.file.Path storedFile;
        String mimeType;
        boolean decrypt;
        if (size != null) {
            if (size.equals("content")) {
                return Response.ok(Strings.nullToEmpty(file.getContent()))
                        .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                        .build();
            }

            storedFile = DirectoryUtil.getStorageDirectory().resolve(fileId + "_" + size);
            mimeType = MimeType.IMAGE_JPEG; // Thumbnails are JPEG
            decrypt = true; // Thumbnails are encrypted
            if (!Files.exists(storedFile)) {
                try {
                    storedFile = Paths.get(getClass().getResource("/image/file-" + size + ".png").toURI());
                } catch (URISyntaxException e) {
                    // Ignore
                }
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
                    
            stream = outputStream -> {
                try {
                    ByteStreams.copy(responseInputStream, outputStream);
                } finally {
                    try {
                        responseInputStream.close();
                        outputStream.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            };
        } catch (Exception e) {
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }

        Response.ResponseBuilder builder = Response.ok(stream)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + file.getFullName("data"))
                .header(HttpHeaders.CONTENT_TYPE, mimeType);
        if (decrypt) {
            // Cache real files
            builder.header(HttpHeaders.CACHE_CONTROL, "private")
                    .header(HttpHeaders.EXPIRES, HttpUtil.buildExpiresHeader(3_600_000L * 24L * 365L));
        } else {
            // Do not cache the temporary thumbnail
            builder.header(HttpHeaders.CACHE_CONTROL, "no-store, must-revalidate")
                    .header(HttpHeaders.EXPIRES, "0");
        }
        return builder.build();
    }

    /**
     * Returns all files from a document, zipped.
     *
     * @api {get} /file/zip Get zipped files
     * @apiName GetFileZip
     * @apiGroup File
     * @apiParam {String} id Document ID
     * @apiParam {String} share Share ID
     * @apiSuccess {Object} file The ZIP file is the whole response
     * @apiError (client) NotFound Document not found
     * @apiError (server) InternalServerError Error creating the ZIP file
     * @apiPermission none
     * @apiVersion 1.5.0
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
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, getTargetIdList(shareId));
        if (documentDto == null) {
            throw new NotFoundException();
        }
        
        // Get files and user associated with this document
        FileDao fileDao = new FileDao();
        final UserDao userDao = new UserDao();
        final List<File> fileList = fileDao.getByDocumentId(principal.getId(), documentId);
        
        // Create the ZIP stream
        StreamingOutput stream = outputStream -> {
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
                        ZipEntry zipEntry = new ZipEntry(index + "-" + file.getFullName(Integer.toString(index)));
                        zipOutputStream.putNextEntry(zipEntry);
                        ByteStreams.copy(decryptedStream, zipOutputStream);
                        zipOutputStream.closeEntry();
                    } catch (Exception e) {
                        throw new WebApplicationException(e);
                    }
                    index++;
                }
            }
            outputStream.close();
        };
        
        // Write to the output
        return Response.ok(stream)
                .header("Content-Type", "application/zip")
                .header("Content-Disposition", "attachment; filename=\"" + documentDto.getTitle().replaceAll("\\W+", "_") + ".zip\"")
                .build();
    }

    /**
     * Find a file with access rights checking.
     *
     * @param fileId File ID
     * @param shareId Share ID
     * @return File
     */
    private File findFile(String fileId, String shareId) {
        FileDao fileDao = new FileDao();
        File file = fileDao.getFile(fileId);
        if (file == null) {
            throw new NotFoundException();
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
            if (!aclDao.checkPermission(file.getDocumentId(), PermType.READ, getTargetIdList(shareId))) {
                throw new ForbiddenClientException();
            }
        }
        return file;
    }
}
