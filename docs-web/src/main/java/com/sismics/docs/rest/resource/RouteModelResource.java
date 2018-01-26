package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.jpa.RouteModelDao;
import com.sismics.docs.core.dao.jpa.criteria.RouteModelCriteria;
import com.sismics.docs.core.dao.jpa.dto.RouteModelDto;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ForbiddenClientException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Route model REST resources.
 * 
 * @author bgamard
 */
@Path("/routemodel")
public class RouteModelResource extends BaseResource {
    /**
     * Returns the list of all route models.
     *
     * @api {get} /routemodel Get route models
     * @apiName GetRouteModel
     * @apiGroup RouteModel
     * @apiParam {Number} sort_column Column index to sort on
     * @apiParam {Boolean} asc If true, sort in ascending order
     * @apiSuccess {Object[]} routemodels List of route models
     * @apiSuccess {String} routemodels.id ID
     * @apiSuccess {String} routemodels.name Name
     * @apiSuccess {Number} routemodels.create_date Create date (timestamp)
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.5.0
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

        JsonArrayBuilder groups = Json.createArrayBuilder();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        RouteModelDao routeModelDao = new RouteModelDao();
        List<RouteModelDto> routeModelDtoList = routeModelDao.findByCriteria(new RouteModelCriteria(), sortCriteria);
        for (RouteModelDto routeModelDto : routeModelDtoList) {
            groups.add(Json.createObjectBuilder()
                    .add("id", routeModelDto.getId())
                    .add("name", routeModelDto.getName())
                    .add("create_date", routeModelDto.getCreateTimestamp()));
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("routemodels", groups);
        return Response.ok().entity(response.build()).build();
    }
}
