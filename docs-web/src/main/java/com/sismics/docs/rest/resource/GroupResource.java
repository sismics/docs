package com.sismics.docs.rest.resource;

import java.text.MessageFormat;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.base.Strings;
import com.sismics.docs.core.dao.jpa.GroupDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.jpa.criteria.GroupCriteria;
import com.sismics.docs.core.dao.jpa.criteria.UserCriteria;
import com.sismics.docs.core.dao.jpa.dto.GroupDto;
import com.sismics.docs.core.dao.jpa.dto.UserDto;
import com.sismics.docs.core.model.jpa.Group;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserGroup;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.JsonUtil;
import com.sismics.rest.util.ValidationUtil;

/**
 * Group REST resources.
 * 
 * @author bgamard
 */
@Path("/group")
public class GroupResource extends BaseResource {
    /**
     * Add a group.
     * 
     * @return Response
     */
    @PUT
    public Response add(@FormParam("parent") String parentName,
            @FormParam("name") String name) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate input
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        ValidationUtil.validateAlphanumeric(name, "name");
        
        // Avoid duplicates
        GroupDao groupDao = new GroupDao();
        Group existingGroup = groupDao.getActiveByName(name);
        if (existingGroup != null) {
            throw new ClientException("GroupAlreadyExists", MessageFormat.format("This group already exists: {0}", name));
        }
        
        // Validate parent
        String parentId = null;
        if (!Strings.isNullOrEmpty(parentName)) {
            Group parentGroup = groupDao.getActiveByName(parentName);
            if (parentGroup == null) {
                throw new ClientException("ParentGroupNotFound", MessageFormat.format("This group does not exists: {0}", parentName));
            }
            parentId = parentGroup.getId();
        }
        
        // Create the group
        groupDao.create(new Group()
                .setName(name)
                .setParentId(parentId), principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Update a group.
     * 
     * @return Response
     */
    @POST
    @Path("{groupName: [a-zA-Z0-9_]+}")
    public Response update(@PathParam("groupName") String groupName,
            @FormParam("parent") String parentName,
            @FormParam("name") String name) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate input
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        ValidationUtil.validateAlphanumeric(name, "name");
        
        // Get the group (by its old name)
        GroupDao groupDao = new GroupDao();
        Group group = groupDao.getActiveByName(groupName);
        if (group == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Avoid duplicates
        Group existingGroup = groupDao.getActiveByName(name);
        if (existingGroup != null && existingGroup.getId() != group.getId()) {
            throw new ClientException("GroupAlreadyExists", MessageFormat.format("This group already exists: {0}", name));
        }
        
        // Validate parent
        String parentId = null;
        if (!Strings.isNullOrEmpty(parentName)) {
            Group parentGroup = groupDao.getActiveByName(parentName);
            if (parentGroup == null) {
                throw new ClientException("ParentGroupNotFound", MessageFormat.format("This group does not exists: {0}", parentName));
            }
            parentId = parentGroup.getId();
        }
        
        // Update the group
        groupDao.update(group.setName(name)
                .setParentId(parentId), principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Delete a group.
     * 
     * @return Response
     */
    @DELETE
    @Path("{groupName: [a-zA-Z0-9_]+}")
    public Response delete(@PathParam("groupName") String groupName) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get the group
        GroupDao groupDao = new GroupDao();
        Group group = groupDao.getActiveByName(groupName);
        if (group == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Delete the group
        groupDao.delete(group.getId(), principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Add a user to a group.
     * 
     * @param groupName Group name
     * @param username Username
     * @return Response
     */
    @PUT
    @Path("{groupName: [a-zA-Z0-9_]+}")
    public Response addMember(@PathParam("groupName") String groupName,
            @FormParam("username") String username) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate input
        groupName = ValidationUtil.validateLength(groupName, "name", 1, 50, false);
        username = ValidationUtil.validateLength(username, "username", 1, 50, false);
        
        // Get the group
        GroupDao groupDao = new GroupDao();
        Group group = groupDao.getActiveByName(groupName);
        if (group == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Get the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Avoid duplicates
        List<GroupDto> groupDtoList = groupDao.findByCriteria(new GroupCriteria().setUserId(user.getId()), null);
        boolean found = false;
        for (GroupDto groupDto : groupDtoList) {
            if (groupDto.getId().equals(group.getId())) {
                found = true;
            }
        }
        
        if (!found) {
            // Add the membership
            UserGroup userGroup = new UserGroup();
            userGroup.setGroupId(group.getId());
            userGroup.setUserId(user.getId());
            groupDao.addMember(userGroup);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Remove an user from a group.
     * 
     * @param groupName Group name
     * @param username Username
     * @return Response
     */
    @DELETE
    @Path("{groupName: [a-zA-Z0-9_]+}/{username: [a-zA-Z0-9_]+}")
    public Response removeMember(@PathParam("groupName") String groupName,
            @PathParam("username") String username) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate input
        groupName = ValidationUtil.validateLength(groupName, "name", 1, 50, false);
        username = ValidationUtil.validateLength(username, "username", 1, 50, false);
        
        // Get the group
        GroupDao groupDao = new GroupDao();
        Group group = groupDao.getActiveByName(groupName);
        if (group == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Get the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Remove the membership
        groupDao.removeMember(group.getId(), user.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all active groups.
     * 
     * @param sortColumn Sort index
     * @param asc If true, ascending sorting, else descending
     * @return Response
     */
    @GET
    public Response list(
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        JsonArrayBuilder groups = Json.createArrayBuilder();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        GroupDao groupDao = new GroupDao();
        List<GroupDto> groupDtoList = groupDao.findByCriteria(new GroupCriteria(), sortCriteria);
        for (GroupDto groupDto : groupDtoList) {
            groups.add(Json.createObjectBuilder()
                    .add("name", groupDto.getName())
                    .add("parent", JsonUtil.nullable(groupDto.getParentName())));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("groups", groups);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Get a group.
     * 
     * @param groupName Group name
     * @return Response
     */
    @GET
    @Path("{groupName: [a-zA-Z0-9_]+}")
    public Response get(@PathParam("groupName") String groupName) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get the group
        GroupDao groupDao = new GroupDao();
        Group group = groupDao.getActiveByName(groupName);
        if (group == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Build the response
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("name", group.getName());
        
        // Get the parent
        if (group.getParentId() != null) {
            Group parentGroup = groupDao.getActiveById(group.getParentId());
            response.add("parent", parentGroup.getName());
        }
        
        // Members
        JsonArrayBuilder members = Json.createArrayBuilder();
        UserDao userDao = new UserDao();
        List<UserDto> userDtoList = userDao.findByCriteria(new UserCriteria().setGroupId(group.getId()), new SortCriteria(1, true));
        for (UserDto userDto : userDtoList) {
            members.add(userDto.getUsername());
        }
        response.add("members", members);
        
        return Response.ok().entity(response.build()).build();
    }
}
