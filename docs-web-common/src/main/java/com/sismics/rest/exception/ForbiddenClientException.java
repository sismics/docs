package com.sismics.rest.exception;

import jakarta.json.Json;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Unauthorized access to the resource exception.
 *
 * @author jtremeaux
 */
public class ForbiddenClientException extends WebApplicationException {
    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor of ForbiddenClientException.
     */
    public ForbiddenClientException() {
        super(Response.status(Status.FORBIDDEN).entity(Json.createObjectBuilder()
            .add("type", "ForbiddenError")
            .add("message", "You don't have access to this resource").build()).build());
    }
}
