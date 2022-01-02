package com.sismics.docs.rest.resource;

import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
