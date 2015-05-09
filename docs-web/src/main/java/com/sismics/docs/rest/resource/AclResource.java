package com.sismics.docs.rest.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.docs.core.constant.AclTargetType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.jpa.criteria.UserCriteria;
import com.sismics.docs.core.dao.jpa.dto.UserDto;
import com.sismics.docs.core.model.jpa.Acl;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * ACL REST resources.
 * 
 * @author bgamard
 */
@Path("/acl")
public class AclResource extends BaseResource {
    /**
     * Add an ACL.
     * 
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(@FormParam("source") String sourceId,
            @FormParam("perm") String permStr,
            @FormParam("username") String username) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        sourceId = ValidationUtil.validateLength(sourceId, "source", 36, 36, false);
        PermType perm = PermType.valueOf(ValidationUtil.validateLength(permStr, "perm", 1, 30, false));
        username = ValidationUtil.validateLength(username, "username", 1, 50, false);
        
        // Validate the target user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", MessageFormat.format("User not found: {0}", username));
        }
        
        // Check permission on the source by the principal
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(sourceId, PermType.WRITE, principal.getId())) {
            throw new ForbiddenClientException();
        }
        
        // Create the ACL
        Acl acl = new Acl();
        acl.setSourceId(sourceId);
        acl.setPerm(perm);
        acl.setTargetId(user.getId());
        
        // Avoid duplicates
        if (!aclDao.checkPermission(acl.getSourceId(), acl.getPerm(), acl.getTargetId())) {
            aclDao.create(acl);
            
            // Returns the ACL
            JSONObject response = new JSONObject();
            response.put("perm", acl.getPerm().name());
            response.put("id", acl.getTargetId());
            response.put("name", user.getUsername());
            response.put("type", AclTargetType.USER.name());
            return Response.ok().entity(response).build();
        }
        
        return Response.ok().entity(new JSONObject()).build();
    }
    
    /**
     * Deletes an ACL.
     * 
     * @param id ACL ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{sourceId: [a-z0-9\\-]+}/{perm: READ|WRITE}/{targetId: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("sourceId") String sourceId,
            @PathParam("perm") String permStr,
            @PathParam("targetId") String targetId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        sourceId = ValidationUtil.validateLength(sourceId, "source", 36, 36, false);
        PermType perm = PermType.valueOf(ValidationUtil.validateLength(permStr, "perm", 1, 30, false));
        targetId = ValidationUtil.validateLength(targetId, "target", 36, 36, false);

        // Check permission on the source by the principal
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(sourceId, PermType.WRITE, principal.getId())) {
            throw new ForbiddenClientException();
        }
        
        // Cannot delete R/W on a source document if the target is the creator
        DocumentDao documentDao = new DocumentDao();
        Document document = documentDao.getById(sourceId);
        if (document != null && document.getUserId().equals(targetId)) {
            throw new ClientException("AclError", "Cannot delete base ACL on a document");
        }
        
        // Delete the ACL
        aclDao.delete(sourceId, perm, targetId);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    @GET
    @Path("target/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response targetList(@QueryParam("search") String search) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        search = ValidationUtil.validateLength(search, "search", 1, 50, false);
        
        // Search users
        UserDao userDao = new UserDao();
        JSONObject response = new JSONObject();
        List<JSONObject> users = new ArrayList<>();
        
        PaginatedList<UserDto> paginatedList = PaginatedLists.create();
        SortCriteria sortCriteria = new SortCriteria(1, true);

        userDao.findByCriteria(paginatedList, new UserCriteria().setSearch(search), sortCriteria);
        for (UserDto userDto : paginatedList.getResultList()) {
            JSONObject user = new JSONObject();
            user.put("username", userDto.getUsername());
            users.add(user);
        }
        
        response.put("users", users);
        return Response.ok().entity(response).build();
    }
}
