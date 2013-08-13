package com.sismics.docs.rest.resource;


import java.text.MessageFormat;

import javax.persistence.NoResultException;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.FileShareDao;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.FileShare;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * File share REST resources.
 * 
 * @author bgamard
 */
@Path("/fileshare")
public class FileShareResource extends BaseResource {
    /**
     * Add a file share to a file.
     *
     * @param fileId File ID
     * @param fileBodyPart File to add
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("id") String fileId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        ValidationUtil.validateRequired(fileId, "id");

        // Get the file
        FileDao fileDao = new FileDao();
        DocumentDao documentDao = new DocumentDao();
        try {
            File file = fileDao.getFile(fileId);
            documentDao.getDocument(file.getDocumentId(), principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("FileNotFound", MessageFormat.format("File not found: {0}", fileId));
        }
        
        // Create the file share
        FileShareDao fileShareDao = new FileShareDao();
        FileShare fileShare = new FileShare();
        fileShare.setFileId(fileId);
        fileShareDao.create(fileShare);

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        response.put("id", fileShare.getId());
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a file share.
     *
     * @param id File share ID
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

        // Get the file share
        FileShareDao fileShareDao = new FileShareDao();
        FileDao fileDao = new FileDao();
        DocumentDao documentDao = new DocumentDao();
        FileShare fileShare;
        try {
            fileShare = fileShareDao.getFileShare(id);
            File file = fileDao.getFile(fileShare.getFileId());
            documentDao.getDocument(file.getDocumentId(), principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("FileShareNotFound", MessageFormat.format("File share not found: {0}", id));
        }

        // Delete the file share
        fileShareDao.delete(fileShare.getId());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
