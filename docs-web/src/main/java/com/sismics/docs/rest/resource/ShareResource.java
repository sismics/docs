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
import com.sismics.docs.core.dao.jpa.ShareDao;
import com.sismics.docs.core.model.jpa.Share;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Share REST resources.
 * 
 * @author bgamard
 */
@Path("/share")
public class ShareResource extends BaseResource {
    /**
     * Add a share to a document.
     *
     * @param documentId Document ID
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("id") String documentId,
            @FormParam("name") String name) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        ValidationUtil.validateRequired(documentId, "id");
        name = ValidationUtil.validateLength(name, "name", 1, 36, true);

        // Get the document
        DocumentDao documentDao = new DocumentDao();
        try {
            documentDao.getDocument(documentId, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", documentId));
        }
        
        // Create the share
        ShareDao shareDao = new ShareDao();
        Share share = new Share();
        share.setDocumentId(documentId);
        share.setName(name);
        shareDao.create(share);

        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        response.put("id", share.getId());
        return Response.ok().entity(response).build();
    }

    /**
     * Deletes a share.
     *
     * @param id Share ID
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

        // Get the share
        ShareDao shareDao = new ShareDao();
        DocumentDao documentDao = new DocumentDao();
        Share share = shareDao.getShare(id);
        if (share == null) {
            throw new ClientException("ShareNotFound", MessageFormat.format("Share not found: {0}", id));
        }
        
        // Check that the user is the owner of the linked document
        try {
            documentDao.getDocument(share.getDocumentId(), principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", share.getDocumentId()));
        }

        // Delete the share
        shareDao.delete(share.getId());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
