package com.sismics.docs.rest.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.docs.core.dao.jpa.TagDao;
import com.sismics.docs.core.model.jpa.Tag;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
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
     * @throws JSONException
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        TagDao tagDao = new TagDao();
        List<Tag> tagList = tagDao.getByUserId(principal.getId());
        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
        for (Tag tag : tagList) {
            JSONObject item = new JSONObject();
            item.put("id", tag.getId());
            item.put("name", tag.getName());
            items.add(item);
        }
        response.put("tags", items);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Creates a new tag.
     * 
     * @param name Name
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("name") String name) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 36, false);
        
        // Get the tag
        TagDao tagDao = new TagDao();
        Tag tag = tagDao.getByUserIdAndName(principal.getId(), name);
        if (tag != null) {
            throw new ClientException("AlreadyExistingTag", MessageFormat.format("Tag already exists: {0}", name));
        }
        
        // Create the tag
        tag = new Tag();
        tag.setName(name);
        tag.setUserId(principal.getId());
        String tagId = tagDao.create(tag);
        
        JSONObject response = new JSONObject();
        response.put("id", tagId);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Update a tag tag.
     * 
     * @param name Name
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String id,
            @FormParam("name") String name) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 36, false);
        
        // Get the tag
        TagDao tagDao = new TagDao();
        Tag tag = tagDao.getByUserIdAndTagId(principal.getId(), id);
        if (tag == null) {
            throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", id));
        }
        
        // Update the tag
        tag.setName(name);
        
        JSONObject response = new JSONObject();
        response.put("id", id);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Delete a tag.
     * 
     * @param name Name
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String tagId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the tag
        TagDao tagDao = new TagDao();
        Tag tag = tagDao.getByUserIdAndTagId(principal.getId(), tagId);
        if (tag == null) {
            throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
        }
        
        // Delete the tag
        tagDao.delete(tagId);
        
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
