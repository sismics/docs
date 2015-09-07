package com.sismics.docs.rest.resource;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.sismics.docs.core.dao.jpa.LocaleDao;
import com.sismics.docs.core.model.jpa.Locale;

/**
 * Locale REST resources.
 * 
 * @author jtremeaux
 */
@Path("/locale")
public class LocaleResource extends BaseResource {
    /**
     * Returns the list of all locales.
     * 
     * @return Response
     */
    @GET
    public Response list() {
        LocaleDao localeDao = new LocaleDao();
        List<Locale> localeList = localeDao.findAll();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (Locale locale : localeList) {
            items.add(Json.createObjectBuilder()
                    .add("id", locale.getId()));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("locales", items);
        return Response.ok().entity(response.build()).build();
    }
}
