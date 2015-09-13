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
     * Returns the list of all tags.
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
        List<Tag> tagList = tagDao.getByUserId(principal.getId());
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (Tag tag : tagList) {
            items.add(Json.createObjectBuilder()
                    .add("id", tag.getId())
                    .add("name", tag.getName())
                    .add("color", tag.getColor())
                    .add("parent", JsonUtil.nullable(tag.getParentId())));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("tags", items);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns stats on tags.
     * 
     * @return Response
     * @throws JSONException
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
        Tag tag = tagDao.getByName(principal.getId(), name);
        if (tag != null) {
            throw new ClientException("AlreadyExistingTag", MessageFormat.format("Tag already exists: {0}", name));
        }
        
        // Check the parent
        if (parentId != null) {
            Tag parentTag = tagDao.getByTagId(principal.getId(), parentId);
            if (parentTag == null) {
                throw new ClientException("ParentNotFound", MessageFormat.format("Parent not found: {0}", parentId));
            }
        }
        
        // Create the tag
        tag = new Tag();
        tag.setName(name);
        tag.setColor(color);
        tag.setUserId(principal.getId());
        tag.setParentId(parentId);
        String id = tagDao.create(tag);
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Update a tag.
     * 
     * @param name Name
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
        Tag tag = tagDao.getByTagId(principal.getId(), id);
        if (tag == null) {
            throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", id));
        }
        
        // Check the parent
        if (parentId != null) {
            Tag parentTag = tagDao.getByTagId(principal.getId(), parentId);
            if (parentTag == null) {
                throw new ClientException("ParentNotFound", MessageFormat.format("Parent not found: {0}", parentId));
            }
        }
        
        // Check for name duplicate
        Tag tagDuplicate = tagDao.getByName(principal.getId(), name);
        if (tagDuplicate != null && !tagDuplicate.getId().equals(id)) {
            throw new ClientException("AlreadyExistingTag", MessageFormat.format("Tag already exists: {0}", name));
        }
        
        // Update the tag
        if (!StringUtils.isEmpty(name)) {
            tag.setName(name);
        }
        if (!StringUtils.isEmpty(color)) {
            tag.setColor(color);
        }
        // Parent tag is always updated to have the possibility to delete it
        tag.setParentId(parentId);
        
        tagDao.update(tag);
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Delete a tag.
     * 
     * @param tagId Tag ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(
            @PathParam("id") String tagId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the tag
        TagDao tagDao = new TagDao();
        Tag tag = tagDao.getByTagId(principal.getId(), tagId);
        if (tag == null) {
            throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
        }
        
        // Delete the tag
        tagDao.delete(tagId);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
