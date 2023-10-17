package com.sismics.docs.rest.resource;

import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Webhook REST resources.
 * 
 * @author bgamard
 */
@Path("/thirdpartywebhook")
public class ThirdPartyWebhookResource extends BaseResource {
    /**
     * Last payload received.
     */
    private static JsonObject lastPayload;

    /**
     * Add a webhook.
     *
     * @return Response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response webhook(JsonObject request) {
        lastPayload = request;
        return Response.ok().build();
    }

    public static JsonObject getLastPayload() {
        return lastPayload;
    }
}
