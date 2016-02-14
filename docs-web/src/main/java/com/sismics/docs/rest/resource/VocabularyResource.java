package com.sismics.docs.rest.resource;

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
import javax.ws.rs.core.Response.Status;

import com.sismics.docs.core.dao.jpa.VocabularyDao;
import com.sismics.docs.core.model.jpa.Vocabulary;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Vocabulary REST resources.
 * 
 * @author bgamard
 */
@Path("/vocabulary")
public class VocabularyResource extends BaseResource {
    @GET
    @Path("{name: [a-z0-9\\-]+}")
    public Response get(@PathParam("name") String name) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Assemble results
        VocabularyDao vocabularyDao = new VocabularyDao();
        List<Vocabulary> vocabularyList = vocabularyDao.getByName(name);
        JsonArrayBuilder entries = Json.createArrayBuilder();
        for (Vocabulary vocabulary : vocabularyList) {
            entries.add(Json.createObjectBuilder()
                    .add("id", vocabulary.getId())
                    .add("value", vocabulary.getValue())
                    .add("order", vocabulary.getOrder()));
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("entries", entries);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Add a vocabulary.
     * 
     * @param name Name
     * @param value Value
     * @param order Order
     * @return Response
     */
    @PUT
    public Response add(@FormParam("name") String name,
            @FormParam("value") String value,
            @FormParam("order") String orderStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        ValidationUtil.validateRegex(name, "name", "[a-z0-9\\-]+");
        value = ValidationUtil.validateLength(value, "value", 1, 100, false);
        Integer order = ValidationUtil.validateInteger(orderStr, "order");
        
        // Create the vocabulary
        VocabularyDao vocabularyDao = new VocabularyDao();
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setName(name);
        vocabulary.setValue(value);
        vocabulary.setOrder(order);
        vocabularyDao.create(vocabulary);
        
        // Returns the vocabulary
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", vocabulary.getId())
                .add("name", vocabulary.getName())
                .add("value", vocabulary.getValue())
                .add("order", vocabulary.getOrder());
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Update a vocabulary.
     * 
     * @param name Name
     * @param value Value
     * @param order Order
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response update(@PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("value") String value,
            @FormParam("order") String orderStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 50, true);
        ValidationUtil.validateRegex(name, "name", "[a-z0-9\\-]+");
        value = ValidationUtil.validateLength(value, "value", 1, 100, true);
        Integer order = null;
        if (orderStr != null) {
            order = ValidationUtil.validateInteger(orderStr, "order");
        }
        
        // Get the vocabulary entry
        VocabularyDao vocabularyDao = new VocabularyDao();
        Vocabulary vocabulary = vocabularyDao.getById(id);
        if (vocabulary == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Update the vocabulary
        if (name != null) {
            vocabulary.setName(name);
        }
        if (value != null) {
            vocabulary.setValue(value);
        }
        if (order != null) {
            vocabulary.setOrder(order);
        }
        vocabularyDao.update(vocabulary);
        
        // Returns the vocabulary
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", vocabulary.getId())
                .add("name", vocabulary.getName())
                .add("value", vocabulary.getValue())
                .add("order", vocabulary.getOrder());
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Delete a vocabulary.
     * 
     * @param id ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the vocabulary
        VocabularyDao vocabularyDao = new VocabularyDao();
        Vocabulary vocabulary = vocabularyDao.getById(id);
        if (vocabulary == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Delete the vocabulary
        vocabularyDao.delete(id);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
