package com.sismics.docs.rest.resource;

import java.text.MessageFormat;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

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
     */
    @PUT
    public Response add(@FormParam("source") String sourceId,
            @FormParam("perm") String permStr,
            @FormParam("username") String username) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        ValidationUtil.validateRequired(sourceId, "source");
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
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("perm", acl.getPerm().name())
                    .add("id", acl.getTargetId())
                    .add("name", user.getUsername())
                    .add("type", AclTargetType.USER.name());
            return Response.ok().entity(response.build()).build();
        }
        
        return Response.ok().entity(Json.createObjectBuilder().build()).build();
    }
    
    /**
     * Deletes an ACL.
     * 
     * @param id ACL ID
     * @return Response
     */
    @DELETE
    @Path("{sourceId: [a-z0-9\\-]+}/{perm: [A-Z]+}/{targetId: [a-z0-9\\-]+}")
    public Response delete(
            @PathParam("sourceId") String sourceId,
            @PathParam("perm") String permStr,
            @PathParam("targetId") String targetId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        ValidationUtil.validateRequired(sourceId, "source");
        PermType perm = PermType.valueOf(ValidationUtil.validateLength(permStr, "perm", 1, 30, false));
        ValidationUtil.validateRequired(targetId, "target");

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
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Search possible ACL target.
     * 
     * @param search Search query
     * @return Response
     */
    @GET
    @Path("target/search")
    public Response targetList(@QueryParam("search") String search) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input
        search = ValidationUtil.validateLength(search, "search", 1, 50, false);
        
        // Search users
        UserDao userDao = new UserDao();
        JsonArrayBuilder users = Json.createArrayBuilder();
        
        PaginatedList<UserDto> paginatedList = PaginatedLists.create();
        SortCriteria sortCriteria = new SortCriteria(1, true);

        userDao.findByCriteria(paginatedList, new UserCriteria().setSearch(search), sortCriteria);
        for (UserDto userDto : paginatedList.getResultList()) {
            users.add(Json.createObjectBuilder()
                    .add("username", userDto.getUsername()));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("users", users);
        return Response.ok().entity(response.build()).build();
    }
}
