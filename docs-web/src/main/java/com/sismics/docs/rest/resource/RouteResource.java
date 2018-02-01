package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.AclTargetType;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.constant.RouteStepTransition;
import com.sismics.docs.core.constant.RouteStepType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.RouteDao;
import com.sismics.docs.core.dao.jpa.RouteModelDao;
import com.sismics.docs.core.dao.jpa.RouteStepDao;
import com.sismics.docs.core.dao.jpa.dto.RouteStepDto;
import com.sismics.docs.core.model.jpa.Route;
import com.sismics.docs.core.model.jpa.RouteModel;
import com.sismics.docs.core.model.jpa.RouteStep;
import com.sismics.docs.core.util.RoutingUtil;
import com.sismics.docs.core.util.SecurityUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

import javax.json.*;
import javax.ws.rs.FormParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.StringReader;

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
     * @apiRouteModel Route
     * @apiParam {String} routeModelId Route model ID
     * @apiParam {String} documentId Document ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) InvalidRouteModel Invalid route model
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

        // Create the route
        Route route = new Route()
                .setDocumentId(documentId)
                .setName(routeModel.getName());
        RouteDao routeDao = new RouteDao();
        routeDao.create(route, principal.getId());

        // Create the steps
        RouteStepDao routeStepDao = new RouteStepDao();
        try (JsonReader reader = Json.createReader(new StringReader(routeModel.getSteps()))) {
            JsonArray stepsJson = reader.readArray();
            for (int order = 0; order < stepsJson.size(); order++) {
                JsonObject step = stepsJson.getJsonObject(order);
                JsonObject target = step.getJsonObject("target");
                AclTargetType targetType = AclTargetType.valueOf(target.getString("type"));
                String targetName = target.getString("name");

                RouteStep routeStep = new RouteStep()
                        .setRouteId(route.getId())
                        .setName(step.getString("name"))
                        .setOrder(order)
                        .setType(RouteStepType.valueOf(step.getString("type")))
                        .setTargetId(SecurityUtil.getTargetIdFromName(targetName, targetType));

                if (routeStep.getTargetId() == null) {
                    throw new ClientException("InvalidRouteModel", "A step has an invalid target");
                }

                routeStepDao.create(routeStep);
            }
        }

        // Intialize ACLs on the first step
        RouteStepDto routeStep = routeStepDao.getCurrentStep(documentId);
        RoutingUtil.updateAcl(documentId, routeStep, null, principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Validate the current step of a route.
     *
     * @api {post} /route/validate Validate the current step of a route
     * @apiName PostRouteValidate
     * @apiRouteModel Route
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
        if (!aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(null))) {
            throw new NotFoundException();
        }

        // Get the current step
        RouteStepDao routeStepDao = new RouteStepDao();
        RouteStepDto routeStep = routeStepDao.getCurrentStep(documentId);
        if (routeStep == null) {
            throw new NotFoundException();
        }

        // Check permission to validate this step
        if (!getTargetIdList(null).contains(routeStep.getTargetId())) {
            throw new ForbiddenClientException();
        }

        // Validate data
        ValidationUtil.validateRequired(transitionStr, "transition");
        comment = ValidationUtil.validateLength(comment, "comment", 1, 500, true);
        RouteStepTransition transition = RouteStepTransition.valueOf(transitionStr);
        if (routeStep.getType() == RouteStepType.VALIDATE && transition != RouteStepTransition.VALIDATED
                || routeStep.getType() == RouteStepType.APPROVE && transition != RouteStepTransition.APPROVED && transition != RouteStepTransition.REJECTED) {
            throw new ClientException("ValidationError", "Invalid transition for this route step type");
        }

        // Validate the step and update ACLs
        routeStepDao.endRouteStep(routeStep.getId(), transition, comment, principal.getId());
        RouteStepDto newRouteStep = routeStepDao.getCurrentStep(documentId);
        RoutingUtil.updateAcl(documentId, newRouteStep, routeStep, principal.getId());
        // TODO Send an email to the new route step

        // Always return OK
        // TODO Return if the document is still readable and return the new current step if any
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
