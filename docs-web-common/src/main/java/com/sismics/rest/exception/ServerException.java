package com.sismics.rest.exception;

import jakarta.json.Json;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jersey exception encapsulating an error from the client (INTERNAL_SERVER_ERROR).
 *
 * @author jtremeaux
 */
public class ServerException extends WebApplicationException {
    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ServerException.class);

    /**
     * Constructor of ClientException.
     * 
     * @param type Error type (e.g. DatabaseError)
     * @param message Human readable error message
     * @param e Inner exception
     */
    public ServerException(String type, String message, Exception e) {
        this(type, message);
        log.error(type + ": " + message, e);
    }

    /**
     * Constructor of ClientException.
     * 
     * @param type Error type (e.g. DatabaseError)
     * @param message Human readable error message
     */
    public ServerException(String type, String message) {
        super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(Json.createObjectBuilder()
            .add("type", type)
            .add("message", message).build()).build());
    }
}
