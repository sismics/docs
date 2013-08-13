package com.sismics.rest.exception;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
     * @throws JSONException
     */
    public ServerException(String type, String message, Exception e) throws JSONException {
        this(type, message);
        log.error(type + ": " + message, e);
    }

    /**
     * Constructor of ClientException.
     * 
     * @param type Error type (e.g. DatabaseError)
     * @param message Human readable error message
     * @throws JSONException
     */
    public ServerException(String type, String message) throws JSONException {
        super(Response.status(Status.INTERNAL_SERVER_ERROR).entity(new JSONObject()
            .put("type", type)
            .put("message", message)).build());
    }
}
