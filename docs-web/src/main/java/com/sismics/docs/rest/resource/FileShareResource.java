package com.sismics.docs.rest.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sun.jersey.multipart.FormDataParam;

/**
 * File share REST resources.
 * 
 * @author bgamard
 */
@Path("/share")
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
    @Consumes("multipart/form-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormDataParam("id") String fileId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        ValidationUtil.validateRequired(fileId, "id");

        // Get the file
        // TODO Not implemented

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        response.put("id", fileId);
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a file share.
     *
     * @param id File shqre ID
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

        // Get the file shqre
//        FileShareDao fileShareDao = new FileShareDao();
//        FileShare fileShare;
//        try {
//            fileShare = fileShareDao.getFileShare(id);
//        } catch (NoResultException e) {
//            throw new ClientException("FileNotFound", MessageFormat.format("File not found: {0}", id));
//        }
//
//        // Delete the file share
//        fileShareDao.delete(fileShare.getId());
        
        // TODO Not implemented

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
