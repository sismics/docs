package com.sismics.docs.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.sismics.util.css.Selector;

/**
 * Theme REST resources.
 * 
 * @author bgamard
 */
@Path("/theme")
public class ThemeResource extends BaseResource {
	/**
     * Returns custom CSS stylesheet.
     * 
     * @return Response
     */
    @GET
    @Path("/stylesheet")
    @Produces("text/css")
    public Response stylesheet() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(new Selector("body"));
        return Response.ok().entity(sb.toString()).build();
    }
}
