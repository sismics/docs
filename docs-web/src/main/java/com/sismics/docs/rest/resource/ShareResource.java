package com.sismics.docs.rest.resource;


import java.text.MessageFormat;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sismics.docs.core.constant.AclTargetType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.ShareDao;
import com.sismics.docs.core.model.jpa.Acl;
import com.sismics.docs.core.model.jpa.Share;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.JsonUtil;
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
     */
    @PUT
    public Response add(
            @FormParam("id") String documentId,
            @FormParam("name") String name) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        ValidationUtil.validateRequired(documentId, "id");
        name = ValidationUtil.validateLength(name, "name", 1, 36, true);

        // Get the document
        DocumentDao documentDao = new DocumentDao();
        if (documentDao.getDocument(documentId, PermType.WRITE, principal.getId()) == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Create the share
        ShareDao shareDao = new ShareDao();
        Share share = new Share();
        share.setName(name);
        shareDao.create(share);
        
        // Create the ACL
        AclDao aclDao = new AclDao();
        Acl acl = new Acl();
        acl.setSourceId(documentId);
        acl.setPerm(PermType.READ);
        acl.setTargetId(share.getId());
        aclDao.create(acl, principal.getId());

        // Returns the created ACL
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("perm", acl.getPerm().name())
                .add("id", acl.getTargetId())
                .add("name", JsonUtil.nullable(name))
                .add("type", AclTargetType.SHARE.toString());
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Deletes a share.
     *
     * @param id Share ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(
            @PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Check that the user can share the linked document
        AclDao aclDao = new AclDao();
        List<Acl> aclList = aclDao.getByTargetId(id);
        if (aclList.size() == 0) {
            throw new ClientException("ShareNotFound", MessageFormat.format("Share not found: {0}", id));
        }
        
        Acl acl = aclList.get(0);
        if (!aclDao.checkPermission(acl.getSourceId(), PermType.WRITE, principal.getId())) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", acl.getSourceId()));
        }

        // Delete the share
        ShareDao shareDao = new ShareDao();
        shareDao.delete(id);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
