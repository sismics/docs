package com.sismics.docs.rest.resource;

import com.google.common.collect.Lists;
import com.sismics.docs.core.constant.*;
import com.sismics.docs.core.dao.AclDao;
import com.sismics.docs.core.dao.GroupDao;
import com.sismics.docs.core.dao.RouteModelDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.criteria.RouteModelCriteria;
import com.sismics.docs.core.dao.dto.RouteModelDto;
import com.sismics.docs.core.model.jpa.Acl;
import com.sismics.docs.core.model.jpa.Group;
import com.sismics.docs.core.model.jpa.RouteModel;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.ActionUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.AclUtil;
import com.sismics.rest.util.ValidationUtil;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.StringReader;
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

        JsonArrayBuilder routeModels = Json.createArrayBuilder();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        RouteModelDao routeModelDao = new RouteModelDao();
        List<RouteModelDto> routeModelDtoList = routeModelDao.findByCriteria(new RouteModelCriteria().setTargetIdList(getTargetIdList(null)), sortCriteria);
        for (RouteModelDto routeModelDto : routeModelDtoList) {
            routeModels.add(Json.createObjectBuilder()
                    .add("id", routeModelDto.getId())
                    .add("name", routeModelDto.getName())
                    .add("create_date", routeModelDto.getCreateTimestamp()));
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("routemodels", routeModels);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Add a route model.
     *
     * @api {put} /routemodel Add a route model
     * @apiName PutRouteModel
     * @apiGroup RouteModel
     * @apiParam {String} name Route model name
     * @apiParam {String} steps Steps data in JSON
     * @apiSuccess {String} id Route model ID
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @PUT
    public Response add(@FormParam("name") String name, @FormParam("steps") String steps) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate input
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        steps = ValidationUtil.validateLength(steps, "steps", 1, 5000, false);
        validateRouteModelSteps(steps);

        // Create the route model
        RouteModelDao routeModelDao = new RouteModelDao();
        String id = routeModelDao.create(new RouteModel()
                .setName(name)
                .setSteps(steps), principal.getId());

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

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Validate route model steps.
     *
     * @param steps Route model steps data
     */
    private void validateRouteModelSteps(String steps) {
        UserDao userDao = new UserDao();
        GroupDao groupDao = new GroupDao();

        try (JsonReader reader = Json.createReader(new StringReader(steps))) {
            JsonArray stepsJson = reader.readArray();
            if (stepsJson.isEmpty()) {
                throw new ClientException("ValidationError", "At least one step is required");
            }
            for (int i = 0; i < stepsJson.size(); i++) {
                JsonObject step = stepsJson.getJsonObject(i);
                if (step.size() != 4) {
                    throw new ClientException("ValidationError", "Steps data not valid");
                }

                // Name
                ValidationUtil.validateLength(step.getString("name"), "step.name", 1, 200, false);

                // Type
                String typeStr = step.getString("type");
                RouteStepType type;
                try {
                    type = RouteStepType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    throw new ClientException("ValidationError", typeStr + "is not a valid route step type");
                }

                // Target
                JsonObject target = step.getJsonObject("target");
                if (target.size() != 2) {
                    throw new ClientException("ValidationError", "Step target is not valid");
                }
                AclTargetType targetType;
                String targetTypeStr = target.getString("type");
                String targetName = target.getString("name");
                ValidationUtil.validateRequired(targetName, "step.target.name");
                ValidationUtil.validateRequired(targetTypeStr, "step.target.type");
                try {
                    targetType = AclTargetType.valueOf(targetTypeStr);
                } catch (IllegalArgumentException e) {
                    throw new ClientException("ValidationError", targetTypeStr + " is not a valid ACL target type");
                }
                switch (targetType) {
                    case USER:
                        User user = userDao.getActiveByUsername(targetName);
                        if (user == null) {
                            throw new ClientException("ValidationError", targetName + " is not a valid user");
                        }
                        break;
                    case GROUP:
                        Group group = groupDao.getActiveByName(targetName);
                        if (group == null) {
                            throw new ClientException("ValidationError", targetName + " is not a valid group");
                        }
                        break;
                }

                // Transitions
                List<RouteStepTransition> transitionsNames = Lists.newArrayList();
                JsonArray transitions = step.getJsonArray("transitions");
                if (type == RouteStepType.VALIDATE) {
                    if (transitions.size() != 1) {
                        throw new ClientException("ValidationError", "VALIDATE steps should have one transition");
                    }
                    transitionsNames.add(RouteStepTransition.VALIDATED);
                } else if (type == RouteStepType.APPROVE) {
                    if (transitions.size() != 2) {
                        throw new ClientException("ValidationError", "APPROVE steps should have two transition");
                    }
                    transitionsNames.add(RouteStepTransition.APPROVED);
                    transitionsNames.add(RouteStepTransition.REJECTED);
                }

                for (int j = 0; j < transitions.size(); j++) {
                    // Transition
                    JsonObject transition = transitions.getJsonObject(j);
                    if (transition.size() != 2) {
                        throw new ClientException("ValidationError", "Transition data is not valid");
                    }

                    // Transition name
                    String routeStepTransitionStr = transition.getString("name");
                    ValidationUtil.validateRequired(routeStepTransitionStr, "step.transitions.name");
                    RouteStepTransition routeStepTransition;
                    try {
                        routeStepTransition = RouteStepTransition.valueOf(routeStepTransitionStr);
                    } catch (IllegalArgumentException e) {
                        throw new ClientException("ValidationError", routeStepTransitionStr + " is not a valid route step transition type");
                    }
                    if (!transitionsNames.contains(routeStepTransition)) {
                        throw new ClientException("ValidationError", routeStepTransitionStr + " is not allowed for this step type");
                    }

                    // Actions
                    JsonArray actions = transition.getJsonArray("actions");
                    for (int k = 0; k < actions.size(); k++) {
                        JsonObject action = actions.getJsonObject(k);

                        // Action type
                        String actionTypeStr = action.getString("type");
                        ActionType actionType;
                        ValidationUtil.validateRequired(routeStepTransitionStr, "step.transitions.actions.type");
                        try {
                            actionType = ActionType.valueOf(actionTypeStr);
                        } catch (IllegalArgumentException e) {
                            throw new ClientException("ValidationError", actionTypeStr + " is not a valid action type");
                        }

                        // Validate action
                        try {
                            ActionUtil.validateAction(actionType, action);
                        } catch (Exception e) {
                            throw new ClientException("ValidationError", e.getMessage());
                        }
                    }
                }
            }
        } catch (JsonException e) {
            throw new ClientException("ValidationError", "Steps data not valid");
        }
    }

    /**
     * Update a route model.
     *
     * @api {post} /routemodel/:id Update a route model
     * @apiName PostRouteModel
     * @apiGroup RouteModel
     * @apiParam {String} name Route model name
     * @apiParam {String} steps Steps data in JSON
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Route model not found
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response update(@PathParam("id") String id,
                           @FormParam("name") String name,
                           @FormParam("steps") String steps) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate input
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        steps = ValidationUtil.validateLength(steps, "steps", 1, 5000, false);
        validateRouteModelSteps(steps);

        // Get the route model
        RouteModelDao routeModelDao = new RouteModelDao();
        RouteModel routeModel = routeModelDao.getActiveById(id);
        if (routeModel == null) {
            throw new NotFoundException();
        }

        // Update the route model
        routeModelDao.update(routeModel.setName(name)
                .setSteps(steps), principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Delete a route model.
     *
     * @api {delete} /routemodel/:id Delete a route model
     * @apiName DeleteRouteModel
     * @apiGroup RouteModel
     * @apiParam {String} id Route model ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Route model not found
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get the route model
        RouteModelDao routeModelDao = new RouteModelDao();
        RouteModel routeModel = routeModelDao.getActiveById(id);
        if (routeModel == null) {
            throw new NotFoundException();
        }

        // Delete the route model
        routeModelDao.delete(routeModel.getId(), principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Get a route model.
     *
     * @api {get} /routemodel/:id Get a route model
     * @apiName GetRouteModel
     * @apiGroup RouteModel
     * @apiParam {String} id Route model ID
     * @apiSuccess {String} id Route model ID
     * @apiSuccess {String} name Route model name
     * @apiSuccess {String} create_date Create date (timestamp)
     * @apiSuccess {String} steps Steps data in JSON
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Route model not found
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param id RouteModel name
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    public Response get(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get the route model
        RouteModelDao routeModelDao = new RouteModelDao();
        RouteModel routeModel = routeModelDao.getActiveById(id);
        if (routeModel == null) {
            throw new NotFoundException();
        }

        // Build the response
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", routeModel.getId())
                .add("name", routeModel.getName())
                .add("create_date", routeModel.getCreateDate().getTime())
                .add("steps", routeModel.getSteps());

        // Add ACL
        AclUtil.addAcls(response, id, getTargetIdList(null));

        return Response.ok().entity(response.build()).build();
    }
}
