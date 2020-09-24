package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.VocabularyDao;
import com.sismics.docs.core.model.jpa.Vocabulary;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Vocabulary REST resources.
 * 
 * @author bgamard
 */
@Path("/vocabulary")
public class VocabularyResource extends BaseResource {
    /**
     * Get a vocabulary.
     *
     * @api {get} /vocabulary/:name Get a vocabulary
     * @apiName GetVocabularyName
     * @apiGroup Vocabulary
     * @apiParam {String} name Vocabulary name
     * @apiSuccess {Object[]} entries List of vocabulary entries
     * @apiSuccess {String} entries.id ID
     * @apiSuccess {String} entries.name Name
     * @apiSuccess {String} entries.value Value
     * @apiSuccess {Number} entries.order Order
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param name Name
     * @return Response
     */
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
                    .add("name", vocabulary.getName())
                    .add("value", vocabulary.getValue())
                    .add("order", vocabulary.getOrder()));
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("entries", entries);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Add a vocabulary entry.
     *
     * @api {put} /vocabulary Add a vocabulary entry
     * @apiName PutVocabulary
     * @apiGroup Vocabulary
     * @apiParam {String{1..50}} name Vocabulary name
     * @apiParam {String{1..500}} value Entry value
     * @apiParam {Number} order Entry order
     * @apiSuccess {String} id ID
     * @apiSuccess {String} name Name
     * @apiSuccess {String} value Value
     * @apiSuccess {Number} order Order
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param name Name
     * @param value Value
     * @param orderStr Order
     * @return Response
     */
    @PUT
    public Response add(@FormParam("name") String name,
            @FormParam("value") String value,
            @FormParam("order") String orderStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        ValidationUtil.validateRegex(name, "name", "[a-z0-9\\-]+");
        value = ValidationUtil.validateLength(value, "value", 1, 500, false);
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
     * Update a vocabulary entry.
     *
     * @api {post} /vocabulary/:id Update a vocabulary entry
     * @apiName PostVocabularyId
     * @apiGroup Vocabulary
     * @apiParam {String} id Entry ID
     * @apiParam {String{1..50}} name Vocabulary name
     * @apiParam {String{1..500}} value Entry value
     * @apiParam {Number} order Entry order
     * @apiSuccess {String} id ID
     * @apiSuccess {String} name Name
     * @apiSuccess {String} value Value
     * @apiSuccess {Number} order Order
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Vocabulary not found
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param id ID
     * @param name Name
     * @param value Value
     * @param orderStr Order
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
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 50, true);
        if (name != null) {
            ValidationUtil.validateRegex(name, "name", "[a-z0-9\\-]+");
        }
        value = ValidationUtil.validateLength(value, "value", 1, 500, true);
        Integer order = null;
        if (orderStr != null) {
            order = ValidationUtil.validateInteger(orderStr, "order");
        }
        
        // Get the vocabulary entry
        VocabularyDao vocabularyDao = new VocabularyDao();
        Vocabulary vocabulary = vocabularyDao.getById(id);
        if (vocabulary == null) {
            throw new NotFoundException();
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
     * Delete a vocabulary entry.
     *
     * @api {delete} /vocabulary/:id Delete a vocabulary entry
     * @apiName DeleteVocabularyId
     * @apiGroup Vocabulary
     * @apiParam {String} id Entry ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Vocabulary not found
     * @apiPermission admin
     * @apiVersion 1.5.0
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
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Get the vocabulary
        VocabularyDao vocabularyDao = new VocabularyDao();
        Vocabulary vocabulary = vocabularyDao.getById(id);
        if (vocabulary == null) {
            throw new NotFoundException();
        }
        
        // Delete the vocabulary
        vocabularyDao.delete(id);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
