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
import javax.ws.rs.core.Response;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.criteria.TagCriteria;
import com.sismics.docs.core.dao.jpa.dto.TagDto;
import com.sismics.docs.core.model.jpa.Acl;
import org.apache.commons.lang.StringUtils;

import com.sismics.docs.core.dao.jpa.TagDao;
import com.sismics.docs.core.dao.jpa.dto.TagStatDto;
import com.sismics.docs.core.model.jpa.Tag;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.JsonUtil;
import com.sismics.rest.util.ValidationUtil;

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
     * @return Response
     */
    @GET
    @Path("/list")
    public Response list() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setUserId(principal.getId()), null);
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (TagDto tagDto : tagDtoList) {
            items.add(Json.createObjectBuilder()
                    .add("id", tagDto.getId())
                    .add("name", tagDto.getName())
                    .add("color", tagDto.getColor())
                    .add("parent", JsonUtil.nullable(tagDto.getParentId())));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("tags", items);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns stats on tags.
     * 
     * @return Response
     */
    @GET
    @Path("/stats")
    public Response stats() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        TagDao tagDao = new TagDao();
        List<TagStatDto> tagStatDtoList = tagDao.getStats(principal.getId());
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (TagStatDto tagStatDto : tagStatDtoList) {
            items.add(Json.createObjectBuilder()
                    .add("id", tagStatDto.getId())
                    .add("name", tagStatDto.getName())
                    .add("color", tagStatDto.getColor())
                    .add("parent", JsonUtil.nullable(tagStatDto.getParentId()))
                    .add("count", tagStatDto.getCount()));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("stats", items);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Creates a new tag.
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
        
        // Don't allow spaces
        if (name.contains(" ")) {
            throw new ClientException("SpacesNotAllowed", "Spaces are not allowed in tag name");
        }
        
        // Get the tag
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setUserId(principal.getId()).setName(name), null);
        if (tagDtoList.size() > 0) {
            throw new ClientException("AlreadyExistingTag", MessageFormat.format("Tag already exists: {0}", name));
        }
        
        // Check the parent
        if (StringUtils.isEmpty(parentId)) {
            parentId = null;
        } else {
            tagDtoList = tagDao.findByCriteria(new TagCriteria().setUserId(principal.getId()).setId(parentId), null);
            if (tagDtoList.size() == 0) {
                throw new ClientException("ParentNotFound", MessageFormat.format("Parent not found: {0}", parentId));
            }
            parentId = tagDtoList.get(0).getId();
        }
        
        // Create the tag
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
        acl.setSourceId(id);
        acl.setTargetId(principal.getId());
        aclDao.create(acl, principal.getId());

        // Create write ACL
        acl = new Acl();
        acl.setPerm(PermType.WRITE);
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
        
        // Don't allow spaces
        if (name.contains(" ")) {
            throw new ClientException("SpacesNotAllowed", "Spaces are not allowed in tag name");
        }
        
        // Get the tag
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setUserId(principal.getId()).setId(id), null);
        if (tagDtoList.size() == 0) {
            throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", id));
        }
        
        // Check the parent
        if (StringUtils.isEmpty(parentId)) {
            parentId = null;
        } else {
            tagDtoList = tagDao.findByCriteria(new TagCriteria().setUserId(principal.getId()).setId(parentId), null);
            if (tagDtoList.size() == 0) {
                throw new ClientException("ParentNotFound", MessageFormat.format("Parent not found: {0}", parentId));
            }
            parentId = tagDtoList.get(0).getId();
        }
        
        // Check for name duplicate
        tagDtoList = tagDao.findByCriteria(new TagCriteria().setUserId(principal.getId()).setName(name), null);
        if (tagDtoList.size() > 0 && !tagDtoList.get(0).getId().equals(id)) {
            throw new ClientException("AlreadyExistingTag", MessageFormat.format("Tag already exists: {0}", name));
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
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setUserId(principal.getId()).setId(id), null);
        if (tagDtoList.size() == 0) {
            throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", id));
        }
        
        // Delete the tag
        tagDao.delete(id, principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
