package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.WebhookEvent;
import com.sismics.docs.core.dao.WebhookDao;
import com.sismics.docs.core.dao.criteria.WebhookCriteria;
import com.sismics.docs.core.dao.dto.WebhookDto;
import com.sismics.docs.core.model.jpa.Webhook;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;

/**
 * Webhook REST resources.
 * 
 * @author bgamard
 */
@Path("/webhook")
public class WebhookResource extends BaseResource {
    /**
     * Returns the list of all webhooks.
     *
     * @api {get} /webhook Get webhooks
     * @apiName GetWebhook
     * @apiGroup Webhook
     * @apiSuccess {Object[]} webhooks List of webhooks
     * @apiSuccess {String} webhooks.id ID
     * @apiSuccess {String} webhooks.event Event
     * @apiSuccess {String} webhooks.url URL
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission admin
     * @apiVersion 1.6.0
     *
     * @return Response
     */
    @GET
    public Response list(@QueryParam("document") String documentId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        WebhookDao webhookDao = new WebhookDao();
        JsonArrayBuilder webhooks = Json.createArrayBuilder();
        List<WebhookDto> webhookDtoList = webhookDao.findByCriteria(new WebhookCriteria(), new SortCriteria(2, true));
        for (WebhookDto webhookDto : webhookDtoList) {
            webhooks.add(Json.createObjectBuilder()
                    .add("id", webhookDto.getId())
                    .add("event", webhookDto.getEvent())
                    .add("url", webhookDto.getUrl())
                    .add("create_date", webhookDto.getCreateTimestamp()));
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("webhooks", webhooks);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Add a webhook.
     *
     * @api {put} /webhook Add a webhook
     * @apiDescription Each time the specified event is raised, the webhook URL will be POST-ed with the following JSON payload: {"event": "Event name", "id": "ID of the document or file"}
     * @apiName PutWebhook
     * @apiGroup Webhook
     * @apiParam {String="DOCUMENT_CREATED","DOCUMENT_UPDATED","DOCUMENT_DELETED","FILE_CREATED","FILE_UPDATED","FILE_DELETED"} event Event
     * @apiParam {String} url URL
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission admin
     * @apiVersion 1.6.0
     *
     * @return Response
     */
    @PUT
    public Response add(@FormParam("event") String eventStr,
                        @FormParam("url") String url) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate input
        WebhookEvent event = WebhookEvent.valueOf(ValidationUtil.validateLength(eventStr, "event", 1, 50, false));
        url = ValidationUtil.validateLength(url, "url", 1, 1024, false);

        // Create the webhook
        WebhookDao webhookDao = new WebhookDao();
        webhookDao.create(new Webhook()
                .setUrl(url)
                .setEvent(event));

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Delete a webhook.
     *
     * @api {delete} /webhook/:id Delete a webhook
     * @apiName DeleteWebhook
     * @apiGroup Webhook
     * @apiParam {String} id Webhook ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Webhook not found
     * @apiPermission admin
     * @apiVersion 1.6.0
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

        // Get the webhook
        WebhookDao webhookDao = new WebhookDao();
        Webhook webhook = webhookDao.getActiveById(id);
        if (webhook == null) {
            throw new NotFoundException();
        }

        // Delete the webhook
        webhookDao.delete(webhook.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
