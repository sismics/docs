package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.sismics.docs.core.constant.AclTargetType;
import com.sismics.docs.core.dao.GroupDao;
import com.sismics.docs.core.dao.RoleBaseFunctionDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.criteria.GroupCriteria;
import com.sismics.docs.core.dao.criteria.UserCriteria;
import com.sismics.docs.core.dao.dto.GroupDto;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.model.jpa.Group;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserGroup;
import com.sismics.docs.core.util.RoutingUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.JsonUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

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
     * @api {put} /group Add a group
     * @apiName PutGroup
     * @apiGroup Group
     * @apiParam {String} name Group name
     * @apiParam {String} [parent] Parent group name
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) GroupAlreadyExists This group already exists
     * @apiError (client) ParentGroupNotFound Parent group not found
     * @apiPermission admin
     * @apiVersion 1.5.0
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
     * @api {post} /group/:name Update a group
     * @apiName PostGroup
     * @apiGroup Group
     * @apiParam {String} name Group name
     * @apiParam {String} [parent] Parent group name
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) GroupAlreadyExists This group already exists
     * @apiError (client) ParentGroupNotFound Parent group not found
     * @apiError (client) NotFound Group not found
     * @apiPermission admin
     * @apiVersion 1.5.0
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
            throw new NotFoundException();
        }
        
        // Avoid duplicates
        Group existingGroup = groupDao.getActiveByName(name);
        if (existingGroup != null && !existingGroup.getId().equals(group.getId())) {
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

        // Check that this group is not used in any workflow in case of renaming
        if (!name.equals(groupName)) {
            String routeModelName = RoutingUtil.findRouteModelNameByTargetName(AclTargetType.GROUP, groupName);
            if (routeModelName != null) {
                throw new ClientException("GroupUsedInRouteModel", routeModelName);
            }
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
     * @api {delete} /group/:name Delete a group
     * @apiName DeleteGroup
     * @apiGroup Group
     * @apiParam {String} name Group name
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Group not found
     * @apiError (client) GroupUsedInRouteModel The group is used in a route model
     * @apiPermission admin
     * @apiVersion 1.5.0
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
            throw new NotFoundException();
        }

        // Ensure that the admin group is not deleted
        if (group.getRoleId() != null) {
            RoleBaseFunctionDao roleBaseFunctionDao = new RoleBaseFunctionDao();
            Set<String> baseFunctionSet = roleBaseFunctionDao.findByRoleId(Sets.newHashSet(group.getRoleId()));
            if (baseFunctionSet.contains(BaseFunction.ADMIN.name())) {
                throw new ClientException("ForbiddenError", "The administrators group cannot be deleted");
            }
        }

        // Check that this group is not used in any workflow
        String routeModelName = RoutingUtil.findRouteModelNameByTargetName(AclTargetType.GROUP, groupName);
        if (routeModelName != null) {
            throw new ClientException("GroupUsedInRouteModel", routeModelName);
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
     * @api {put} /group/:name Add a user to a group
     * @apiName PutGroupMember
     * @apiGroup Group
     * @apiParam {String} name Group name
     * @apiParam {String} username Username
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Group or user not found
     * @apiPermission admin
     * @apiVersion 1.5.0
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
            throw new NotFoundException();
        }
        
        // Get the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new NotFoundException();
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
     * @api {delete} /group/:name/:username Remove a user from a group
     * @apiName DeleteGroupMember
     * @apiGroup Group
     * @apiParam {String} name Group name
     * @apiParam {String} username Username
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Group or user not found
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param groupName Group name
     * @param username Username
     * @return Response
     */
    @DELETE
    @Path("{groupName: [a-zA-Z0-9_]+}/{username: [a-zA-Z0-9_@\\.]+}")
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
            throw new NotFoundException();
        }
        
        // Get the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new NotFoundException();
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
     * @api {get} /group Get groups
     * @apiName GetGroupList
     * @apiGroup Group
     * @apiParam {Number} sort_column Column index to sort on
     * @apiParam {Boolean} asc If true, sort in ascending order
     * @apiSuccess {Object[]} groups List of groups
     * @apiSuccess {String} groups.name Name
     * @apiSuccess {String} groups.parent Parent name
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.5.0
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
     * @api {get} /group/:name Get a group
     * @apiName GetGroup
     * @apiGroup Group
     * @apiParam {String} name Group name
     * @apiSuccess {String} name Group name
     * @apiSuccess {String} parent Parent name
     * @apiSuccess {String[]} members List of members
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Group not found
     * @apiPermission user
     * @apiVersion 1.5.0
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
        
        // Get the group
        GroupDao groupDao = new GroupDao();
        Group group = groupDao.getActiveByName(groupName);
        if (group == null) {
            throw new NotFoundException();
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
