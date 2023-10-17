package com.sismics.docs.rest.resource;

import com.google.common.collect.Sets;
import com.sismics.docs.core.constant.AclType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.AclDao;
import com.sismics.docs.core.dao.TagDao;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.dto.TagDto;
import com.sismics.docs.core.model.jpa.Acl;
import com.sismics.docs.core.model.jpa.Tag;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.AclUtil;
import com.sismics.rest.util.ValidationUtil;
import org.apache.commons.lang3.StringUtils;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 * Tag REST resources.
 * 
 * @author bgamard
 */
@Path("/tag")
public class TagResource extends BaseResource {
    /**
     * Returns the list of all visible tags.
     *
     * @api {get} /tag/list Get tags
     * @apiName GetTagList
     * @apiGroup Tag
     * @apiSuccess {Object[]} tags List of tags
     * @apiSuccess {String} tags.id ID
     * @apiSuccess {String} tags.name Name
     * @apiSuccess {String} tags.color Color
     * @apiSuccess {String} tags.parent Parent
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    @Path("/list")
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setTargetIdList(getTargetIdList(null)), new SortCriteria(1, true));

        // Extract tag IDs
        Set<String> tagIdSet = Sets.newHashSet();
        for (TagDto tagDto : tagDtoList) {
            tagIdSet.add(tagDto.getId());
        }

        // Build the response
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (TagDto tagDto : tagDtoList) {
            JsonObjectBuilder item = Json.createObjectBuilder()
                    .add("id", tagDto.getId())
                    .add("name", tagDto.getName())
                    .add("color", tagDto.getColor());
            if (tagIdSet.contains(tagDto.getParentId())) {
                item.add("parent", tagDto.getParentId());
            }
            items.add(item);
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("tags", items);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns a tag.
     *
     * @api {get} /tag/:id Get a tag
     * @apiName GetTag
     * @apiGroup Tag
     * @apiSuccess {String} id ID
     * @apiSuccess {String} name Name
     * @apiSuccess {String} creator Username of the creator
     * @apiSuccess {String} color Color
     * @apiSuccess {String} parent Parent
     * @apiSuccess {Boolean} writable True if the tag is writable by the current user
     * @apiSuccess {Object[]} acls List of ACL
     * @apiSuccess {String} acls.id ID
     * @apiSuccess {String="READ","WRITE"} acls.perm Permission
     * @apiSuccess {String} acls.name Target name
     * @apiSuccess {String="USER","GROUP","SHARE"} acls.type Target type
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Tag not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param id Tag ID
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    public Response get(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setTargetIdList(getTargetIdList(null)).setId(id), null);
        if (tagDtoList.isEmpty()) {
            throw new NotFoundException();
        }

        // Add tag informatiosn
        TagDto tagDto = tagDtoList.get(0);
        JsonObjectBuilder tag = Json.createObjectBuilder()
                .add("id", tagDto.getId())
                .add("creator", tagDto.getCreator())
                .add("name", tagDto.getName())
                .add("color", tagDto.getColor());

        // Add the parent if its visible
        if (tagDto.getParentId() != null) {
            AclDao aclDao = new AclDao();
            if (aclDao.checkPermission(tagDto.getParentId(), PermType.READ, getTargetIdList(null))) {
                tag.add("parent", tagDto.getParentId());
            }
        }

        // Add ACL
        AclUtil.addAcls(tag, id, getTargetIdList(null));

        return Response.ok().entity(tag.build()).build();
    }

    /**
     * Creates a new tag.
     *
     * @api {put} /tag Create a tag
     * @apiName PutTag
     * @apiGroup Tag
     * @apiParam {String} name Name
     * @apiParam {String} color Color
     * @apiParam {String} parent Parent ID
     * @apiSuccess {String} id Tag ID
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) IllegalTagName Spaces and colons are not allowed in tag name
     * @apiError (client) ParentNotFound Parent not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param name Name
     * @param color Color
     * @param parentId Parent ID
     * @return Response
     */
    @PUT
    public Response add(
            @FormParam("name") String name,
            @FormParam("color") String color,
            @FormParam("parent") String parentId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 36, false);
        ValidationUtil.validateHexColor(color, "color", true);
        ValidationUtil.validateTagName(name);

        // Check the parent
        if (StringUtils.isEmpty(parentId)) {
            parentId = null;
        } else {
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(parentId, PermType.READ, getTargetIdList(null))) {
                throw new ClientException("ParentNotFound", MessageFormat.format("Parent not found: {0}", parentId));
            }
        }

        // Create the tag
        TagDao tagDao = new TagDao();
        Tag tag = new Tag();
        tag.setName(name);
        tag.setColor(color);
        tag.setUserId(principal.getId());
        tag.setParentId(parentId);
        String id = tagDao.create(tag, principal.getId());

        // Create read ACL
        AclDao aclDao = new AclDao();
        Acl acl = new Acl();
        acl.setPerm(PermType.READ);
        acl.setType(AclType.USER);
        acl.setSourceId(id);
        acl.setTargetId(principal.getId());
        aclDao.create(acl, principal.getId());

        // Create write ACL
        acl = new Acl();
        acl.setPerm(PermType.WRITE);
        acl.setType(AclType.USER);
        acl.setSourceId(id);
        acl.setTargetId(principal.getId());
        aclDao.create(acl, principal.getId());
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Update a tag.
     *
     * @api {post} /tag/:id Update a tag
     * @apiName PostTag
     * @apiGroup Tag
     * @apiParam {String} id Tag ID
     * @apiParam {String} name Name
     * @apiParam {String} color Color
     * @apiParam {String} parent Parent ID
     * @apiSuccess {String} id Tag ID
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) IllegalTagName Spaces and colons are not allowed in tag name
     * @apiError (client) ParentNotFound Parent not found
     * @apiError (client) CircularReference Circular reference in parent tag
     * @apiError (client) NotFound Tag not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param name Name
     * @param color Color
     * @param parentId Parent ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response update(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("color") String color,
            @FormParam("parent") String parentId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 36, true);
        ValidationUtil.validateHexColor(color, "color", true);
        ValidationUtil.validateTagName(name);

        // Check permission
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(id, PermType.WRITE, getTargetIdList(null))) {
            throw new NotFoundException();
        }
        
        // Check the parent
        TagDao tagDao = new TagDao();
        if (StringUtils.isEmpty(parentId)) {
            parentId = null;
        } else {
            if (!aclDao.checkPermission(parentId, PermType.READ, getTargetIdList(null))) {
                throw new ClientException("ParentNotFound", MessageFormat.format("Parent not found: {0}", parentId));
            }

            String parentTagId = parentId;
            do {
                Tag parentTag = tagDao.getById(parentTagId);
                parentTagId = parentTag.getParentId();
                if (parentTag.getId().equals(id)) {
                    throw new ClientException("CircularReference", "Circular reference in parent tag");
                }
            } while (parentTagId != null);
        }

        // Update the tag
        Tag tag = tagDao.getById(id);
        if (!StringUtils.isEmpty(name)) {
            tag.setName(name);
        }
        if (!StringUtils.isEmpty(color)) {
            tag.setColor(color);
        }
        // Parent tag is always updated to have the possibility to delete it
        tag.setParentId(parentId);
        
        tagDao.update(tag, principal.getId());
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Delete a tag.
     *
     * @api {delete} /tag/:id Delete a tag
     * @apiName DeleteTag
     * @apiGroup Tag
     * @apiParam {String} id Tag ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Tag not found
     * @apiPermission user
     * @apiVersion 1.5.0
     * 
     * @param id Tag ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(
            @PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the tag
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(id, PermType.WRITE, getTargetIdList(null))) {
            throw new NotFoundException();
        }

        // Delete the tag
        TagDao tagDao = new TagDao();
        tagDao.delete(id, principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
