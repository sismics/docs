package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.MetadataType;
import com.sismics.docs.core.dao.MetadataDao;
import com.sismics.docs.core.dao.criteria.MetadataCriteria;
import com.sismics.docs.core.dao.dto.MetadataDto;
import com.sismics.docs.core.model.jpa.Metadata;
import com.sismics.docs.core.util.jpa.SortCriteria;
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
 * Metadata REST resources.
 * 
 * @author bgamard
 */
@Path("/metadata")
public class MetadataResource extends BaseResource {
    /**
     * Returns the list of all configured metadata.
     *
     * @api {get} /metadata Get configured metadata
     * @apiName GetMetadata
     * @apiGroup Metadata
     * @apiParam {Number} sort_column Column index to sort on
     * @apiParam {Boolean} asc If true, sort in ascending order
     * @apiSuccess {Object[]} metadata List of metadata
     * @apiSuccess {String} metadata.id ID
     * @apiSuccess {String} metadata.name Name
     * @apiSuccess {String="STRING","INTEGER","FLOAT","DATE","BOOLEAN"} metadata.type Type
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.7.0
     *
     * @return Response
     */
    @GET
    public Response list(
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        JsonArrayBuilder metadata = Json.createArrayBuilder();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        MetadataDao metadataDao = new MetadataDao();
        List<MetadataDto> metadataDtoList = metadataDao.findByCriteria(new MetadataCriteria(), sortCriteria);
        for (MetadataDto metadataDto : metadataDtoList) {
            metadata.add(Json.createObjectBuilder()
                    .add("id", metadataDto.getId())
                    .add("name", metadataDto.getName())
                    .add("type", metadataDto.getType().name()));
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("metadata", metadata);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Add a metadata.
     *
     * @api {put} /metadata Add a custom metadata
     * @apiName PutMetadata
     * @apiGroup Metadata
     * @apiParam {String{1..50}} name Name
     * @apiParam {String="STRING","INTEGER","FLOAT","DATE","BOOLEAN"} type Type
     * @apiSuccess {String} id ID
     * @apiSuccess {String} name Name
     * @apiSuccess {String="STRING","INTEGER","FLOAT","DATE","BOOLEAN"} type Type
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission admin
     * @apiVersion 1.7.0
     *
     * @param name Name
     * @param typeStr Type
     * @return Response
     */
    @PUT
    public Response add(@FormParam("name") String name,
                        @FormParam("type") String typeStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        MetadataType type = MetadataType.valueOf(ValidationUtil.validateLength(typeStr, "type", 1, 20, false));

        // Create the metadata
        MetadataDao metadataDao = new MetadataDao();
        Metadata metadata = new Metadata();
        metadata.setName(name);
        metadata.setType(type);
        metadataDao.create(metadata, principal.getId());

        // Returns the metadata
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", metadata.getId())
                .add("name", metadata.getName())
                .add("type", metadata.getType().name());
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Update a metadata.
     *
     * @api {post} /metadata/:id Update a custom metadata
     * @apiName PostMetadataId
     * @apiGroup Metadata
     * @apiParam {String} id Metadata ID
     * @apiParam {String{1..50}} name Name
     * @apiSuccess {String} id ID
     * @apiSuccess {String} name Name
     * @apiSuccess {String="STRING","INTEGER","FLOAT","DATE","BOOLEAN"} type Type
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Metadata not found
     * @apiPermission admin
     * @apiVersion 1.7.0
     *
     * @param id ID
     * @param name Name
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response update(@PathParam("id") String id,
                           @FormParam("name") String name) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate input data
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);

        // Get the metadata
        MetadataDao metadataDao = new MetadataDao();
        Metadata metadata = metadataDao.getActiveById(id);
        if (metadata == null) {
            throw new NotFoundException();
        }

        // Update the metadata
        metadata.setName(name);
        metadataDao.update(metadata, principal.getId());

        // Returns the metadata
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", metadata.getId())
                .add("name", metadata.getName())
                .add("type", metadata.getType().name());
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Delete a metadata.
     *
     * @api {delete} /metadata/:id Delete a custom metadata
     * @apiName DeleteMetadataId
     * @apiGroup Metadata
     * @apiParam {String} id Metadata ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Metadata not found
     * @apiPermission admin
     * @apiVersion 1.7.0
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

        // Get the metadata
        MetadataDao metadataDao = new MetadataDao();
        Metadata metadata = metadataDao.getActiveById(id);
        if (metadata == null) {
            throw new NotFoundException();
        }

        // Delete the metadata
        metadataDao.delete(id, principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
