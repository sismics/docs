package com.sismics.docs.rest.resource;

import com.sismics.docs.core.dao.jpa.LocaleDao;
import com.sismics.docs.core.model.jpa.Locale;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() throws JSONException {
        LocaleDao localeDao = new LocaleDao();
        List<Locale> localeList = localeDao.findAll();
        JSONObject response = new JSONObject();
        List<JSONObject> items = new ArrayList<JSONObject>();
        for (Locale locale : localeList) {
            JSONObject item = new JSONObject();
            item.put("id", locale.getId());
            items.add(item);
        }
        response.put("locales", items);
        return Response.ok().entity(response).build();
    }
}
