package com.sismics.docs.rest.resource;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

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
        value = ValidationUtil.validateLength(value, "value", 1, 100, false);
        Integer order = ValidationUtil.validateInteger(orderStr, "order");
        
        // Create the comment
        VocabularyDao vocabularyDao = new VocabularyDao();
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setName(name);
        vocabulary.setValue(value);
        vocabulary.setOrder(order);
        vocabularyDao.create(vocabulary);
        
        // Returns the comment
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", vocabulary.getId())
                .add("name", vocabulary.getName())
                .add("value", vocabulary.getValue())
                .add("order", Integer.toString(vocabulary.getOrder()));
        return Response.ok().entity(response.build()).build();
    }
}
