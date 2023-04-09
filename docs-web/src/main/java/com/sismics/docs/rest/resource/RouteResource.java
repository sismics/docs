package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.*;
import com.sismics.docs.core.dao.*;
import com.sismics.docs.core.dao.criteria.RouteCriteria;
import com.sismics.docs.core.dao.criteria.RouteStepCriteria;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.dao.dto.RouteDto;
import com.sismics.docs.core.dao.dto.RouteStepDto;
import com.sismics.docs.core.model.jpa.Route;
import com.sismics.docs.core.model.jpa.RouteModel;
import com.sismics.docs.core.model.jpa.RouteStep;
import com.sismics.docs.core.util.ActionUtil;
import com.sismics.docs.core.util.RoutingUtil;
import com.sismics.docs.core.util.SecurityUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

import jakarta.json.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.util.List;

/**
 * Route REST resources.
 * 
 * @author bgamard
 */
@Path("/route")
public class RouteResource extends BaseResource {
    /**
     * Start a route on a document.
     *
     * @api {post} /route/start Start a route on a document
     * @apiName PostRouteStart
     * @apiGroup Route
     * @apiParam {String} routeModelId Route model ID
     * @apiParam {String} documentId Document ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) InvalidRouteModel Invalid route model
     * @apiError (client) RunningRoute A running route already exists on this document
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Route model or document not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @POST
    @Path("start")
    public Response start(@FormParam("routeModelId") String routeModelId,
                          @FormParam("documentId") String documentId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the document
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(documentId, PermType.WRITE, getTargetIdList(null))) {
            throw new NotFoundException();
        }

        // Get the route model
        RouteModelDao routeModelDao = new RouteModelDao();
        RouteModel routeModel = routeModelDao.getActiveById(routeModelId);
        if (routeModel == null) {
            throw new NotFoundException();
        }

        // Check permission on this route model
        if (!aclDao.checkPermission(routeModelId, PermType.READ, getTargetIdList(null))) {
            throw new ForbiddenClientException();
        }

        // Avoid creating 2 running routes on the same document
        RouteStepDao routeStepDao = new RouteStepDao();
        if (routeStepDao.getCurrentStep(documentId) != null) {
            throw new ClientException("RunningRoute", "A running route already exists on this document");
        }

        // Create the route
        Route route = new Route()
                .setDocumentId(documentId)
                .setName(routeModel.getName());
        RouteDao routeDao = new RouteDao();
        routeDao.create(route, principal.getId());

        // Create the steps
        try (JsonReader reader = Json.createReader(new StringReader(routeModel.getSteps()))) {
            JsonArray stepsJson = reader.readArray();
            for (int order = 0; order < stepsJson.size(); order++) {
                JsonObject step = stepsJson.getJsonObject(order);
                JsonObject target = step.getJsonObject("target");
                AclTargetType targetType = AclTargetType.valueOf(target.getString("type"));
                String targetName = target.getString("name");
                String transitions = null;
                if (step.containsKey("transitions")) {
                    transitions = step.getJsonArray("transitions").toString();
                }

                RouteStep routeStep = new RouteStep()
                        .setRouteId(route.getId())
                        .setName(step.getString("name"))
                        .setOrder(order)
                        .setType(RouteStepType.valueOf(step.getString("type")))
                        .setTransitions(transitions)
                        .setTargetId(SecurityUtil.getTargetIdFromName(targetName, targetType));

                if (routeStep.getTargetId() == null) {
                    throw new ClientException("InvalidRouteModel", "A step has an invalid target");
                }

                routeStepDao.create(routeStep);
            }
        }

        // Intialize ACLs on the first step
        RouteStepDto routeStepDto = routeStepDao.getCurrentStep(documentId);
        RoutingUtil.updateAcl(documentId, routeStepDto, null, principal.getId());
        RoutingUtil.sendRouteStepEmail(documentId, routeStepDto);

        JsonObjectBuilder step = routeStepDto.toJson();
        step.add("transitionable", getTargetIdList(null).contains(routeStepDto.getTargetId()));
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("route_step", step);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Validate the current step of a route.
     *
     * @api {post} /route/validate Validate the current step of a route
     * @apiName PostRouteValidate
     * @apiGroup Route
     * @apiParam {String} documentId Document ID
     * @apiParam {String} transition Route step transition
     * @apiParam {String} comment Route step comment
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Document or route not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @POST
    @Path("validate")
    public Response validate(@FormParam("documentId") String documentId,
                             @FormParam("transition") String transitionStr,
                             @FormParam("comment") String comment) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the document
        AclDao aclDao = new AclDao();
        DocumentDao documentDao = new DocumentDao();
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, getTargetIdList(null));
        if (documentDto == null) {
            throw new NotFoundException();
        }

        // Get the current step
        RouteStepDao routeStepDao = new RouteStepDao();
        RouteStepDto routeStepDto = routeStepDao.getCurrentStep(documentId);
        if (routeStepDto == null) {
            throw new NotFoundException();
        }

        // Check permission to validate this step
        if (!getTargetIdList(null).contains(routeStepDto.getTargetId())) {
            throw new ForbiddenClientException();
        }

        // Validate data
        ValidationUtil.validateRequired(transitionStr, "transition");
        comment = ValidationUtil.validateLength(comment, "comment", 1, 500, true);
        RouteStepTransition routeStepTransition = RouteStepTransition.valueOf(transitionStr);
        if (routeStepDto.getType() == RouteStepType.VALIDATE && routeStepTransition != RouteStepTransition.VALIDATED
                || routeStepDto.getType() == RouteStepType.APPROVE
                && routeStepTransition != RouteStepTransition.APPROVED && routeStepTransition != RouteStepTransition.REJECTED) {
            throw new ClientException("ValidationError", "Invalid transition for this route step type");
        }

        // Execute actions
        if (routeStepDto.getTransitions() != null) {
            try (JsonReader reader = Json.createReader(new StringReader(routeStepDto.getTransitions()))) {
                JsonArray transitions = reader.readArray();
                // Filter out our transition
                for (int i = 0; i < transitions.size(); i++) {
                    JsonObject transition = transitions.getJsonObject(i);
                    if (transition.getString("name").equals(routeStepTransition.name())) {
                        // Transition found, execute those actions
                        JsonArray actions = transition.getJsonArray("actions");
                        for (int j = 0; j < actions.size(); j++) {
                            JsonObject action = actions.getJsonObject(j);
                            ActionType actionType = ActionType.valueOf(action.getString("type"));
                            ActionUtil.executeAction(actionType, action, documentDto);
                        }
                    }
                }
            }
        }

        // Validate the step and update ACLs
        routeStepDao.endRouteStep(routeStepDto.getId(), routeStepTransition, comment, principal.getId());
        RouteStepDto newRouteStep = routeStepDao.getCurrentStep(documentId);
        RoutingUtil.updateAcl(documentId, newRouteStep, routeStepDto, principal.getId());
        if (newRouteStep != null) {
            RoutingUtil.sendRouteStepEmail(documentId, newRouteStep);
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("readable", aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(null)));
        if (newRouteStep != null) {
            JsonObjectBuilder step = newRouteStep.toJson();
            step.add("transitionable", getTargetIdList(null).contains(newRouteStep.getTargetId()));
            response.add("route_step", step);
        }
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns the routes on a document.
     *
     * @api {get} /route Get the routes on a document
     * @apiName GetRoutes
     * @apiGroup Route
     * @apiParam {String} documentId Document ID
     * @apiSuccess {Object[]} routes List of routes
     * @apiSuccess {String} routes.name Name
     * @apiSuccess {Number} routes.create_date Create date (timestamp)
     * @apiSuccess {Object[]} routes.steps Route steps
     * @apiSuccess {String} routes.steps.name Route step name
     * @apiSuccess {String="APPROVE", "VALIDATE"} routes.steps.type Route step type
     * @apiSuccess {String} routes.steps.comment Route step comment
     * @apiSuccess {Number} routes.steps.end_date Route step end date (timestamp)
     * @apiSuccess {String="APPROVED","REJECTED","VALIDATED"} routes.steps.transition Route step transition
     * @apiSuccess {Object} routes.steps.validator_username Validator username
     * @apiSuccess {Object} routes.steps.target Route step target
     * @apiSuccess {String} routes.steps.target.id Route step target ID
     * @apiSuccess {String} routes.steps.target.name Route step target name
     * @apiSuccess {String="USER","GROUP"} routes.steps.target.type Route step target type
     * @apiError (client) NotFound Document not found
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param documentId Document ID
     * @return Response
     */
    @GET
    public Response get(@QueryParam("documentId") String documentId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        DocumentDao documentDao = new DocumentDao();
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, getTargetIdList(null));
        if (documentDto == null) {
            throw new NotFoundException();
        }

        JsonArrayBuilder routes = Json.createArrayBuilder();

        RouteDao routeDao = new RouteDao();
        RouteStepDao routeStepDao = new RouteStepDao();
        List<RouteDto> routeDtoList = routeDao.findByCriteria(new RouteCriteria()
                .setDocumentId(documentId), new SortCriteria(2, false));
        for (RouteDto routeDto : routeDtoList) {
            List<RouteStepDto> routeStepDtoList = routeStepDao.findByCriteria(new RouteStepCriteria()
                    .setRouteId(routeDto.getId()), new SortCriteria(6, true));
            JsonArrayBuilder steps = Json.createArrayBuilder();

            for (RouteStepDto routeStepDto : routeStepDtoList) {
                steps.add(routeStepDto.toJson());
            }

            routes.add(Json.createObjectBuilder()
                    .add("name", routeDto.getName())
                    .add("create_date", routeDto.getCreateTimestamp())
                    .add("steps", steps));
        }

        JsonObjectBuilder json = Json.createObjectBuilder()
                .add("routes", routes);
        return Response.ok().entity(json.build()).build();
    }

    /**
     * Cancel a route.
     *
     * @api {delete} /route Cancel a route
     * @apiName DeleteRoute
     * @apiGroup Route
     * @apiParam {String} documentId Document ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Document or route not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @DELETE
    public Response delete(@QueryParam("documentId") String documentId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the document
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(documentId, PermType.WRITE, getTargetIdList(null))) {
            throw new NotFoundException();
        }

        // Get the current step
        RouteStepDao routeStepDao = new RouteStepDao();
        RouteStepDto routeStepDto = routeStepDao.getCurrentStep(documentId);
        if (routeStepDto == null) {
            throw new NotFoundException();
        }

        // Remove the temporary ACLs
        RoutingUtil.updateAcl(documentId, null, routeStepDto, principal.getId());

        // Delete the route and the steps
        RouteDao routeDao = new RouteDao();
        routeDao.deleteRoute(routeStepDto.getRouteId(), principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
