package com.sismics.docs.rest.resource;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.dao.jpa.ConfigDao;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.JsonUtil;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.css.Selector;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.StringReader;
import java.util.Map;

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
        JsonObject themeConfig = getThemeConfig();

        // Build the stylesheet
    	StringBuilder sb = new StringBuilder();
    	sb.append(new Selector(".navbar")
            .rule("background-color", themeConfig.getString("color", "#263238")));
        sb.append(themeConfig.getString("css", ""));

        return Response.ok().entity(sb.toString()).build();
    }

    /**
     * Returns the theme configuration.
     *
     * @return Response
     */
    @GET
    public Response get() {
        JsonObject themeConfig = getThemeConfig();
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("name", themeConfig.getString("name", "Sismics Docs"));
        return Response.ok().entity(json.build()).build();
    }

    /**
     * Change the theme configuration.
     *
     * @param color Theme color
     * @param name Application name
     * @param css Custom CSS
     * @return Response
     */
    @POST
    public Response theme(@FormParam("color") String color,
              @FormParam("name") String name,
              @FormParam("css") String css) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate input data
        ValidationUtil.validateHexColor(color, "color", true);
        name = ValidationUtil.validateLength(name, "name", 3, 30, true);

        // Update the JSON
        JsonObjectBuilder json = getMutableThemeConfig();
        if (Strings.isNullOrEmpty(color)) {
            json.add("color", JsonValue.NULL);
        } else {
            json.add("color", color);
        }
        if (Strings.isNullOrEmpty(name)) {
            json.add("name", JsonValue.NULL);
        } else {
            json.add("name", name);
        }
        json.add("css", JsonUtil.nullable(css));

        // Persist the new configuration
        ConfigDao configDao = new ConfigDao();
        configDao.update(ConfigType.THEME, json.build().toString());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    @PUT
    @Path("images")
    @Consumes("multipart/form-data")
    public Response images(
            @FormDataParam("logo") FormDataBodyPart logoBodyPart,
            @FormDataParam("background") FormDataBodyPart backgrounBodyPart) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        return Response.ok().build();
    }

    /**
     * Returns the theme configuration object.
     *
     * @return Theme configuration
     */
    private JsonObject getThemeConfig() {
        ConfigDao configDao = new ConfigDao();
        Config themeConfig = configDao.getById(ConfigType.THEME);
        if (themeConfig == null) {
            return Json.createObjectBuilder().build();
        }

        try (JsonReader reader = Json.createReader(new StringReader(themeConfig.getValue()))) {
            return reader.readObject();
        }
    }

    /**
     * Returns a mutable theme configuration.
     *
     * @return Json builder
     */
    private JsonObjectBuilder getMutableThemeConfig() {
        JsonObject themeConfig = getThemeConfig();
        JsonObjectBuilder json = Json.createObjectBuilder();

        for (Map.Entry<String, JsonValue> entry : themeConfig.entrySet()) {
            json.add(entry.getKey(), entry.getValue());
        }

        return json;
    }
}
