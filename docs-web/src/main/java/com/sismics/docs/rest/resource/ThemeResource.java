package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.dao.ConfigDao;
import com.sismics.docs.core.model.jpa.Config;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.HttpUtil;
import com.sismics.util.JsonUtil;
import com.sismics.util.css.Selector;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
     * @api {get} /theme/stylesheet Get the CSS stylesheet
     * @apiName GetThemeStylesheet
     * @apiGroup Theme
     * @apiSuccess {String} stylesheet The whole response is the stylesheet
     * @apiPermission none
     * @apiVersion 1.5.0
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
            .rule("background-color", themeConfig.getString("color", "#ffffff")));
        sb.append(themeConfig.getString("css", ""));

        return Response.ok().entity(sb.toString()).build();
    }

    /**
     * Returns the theme configuration.
     *
     * @api {get} /theme Get the theme configuration
     * @apiName GetTheme
     * @apiGroup Theme
     * @apiSuccess {String} name Application name
     * @apiSuccess {String} color Main color
     * @apiSuccess {String} css Custom CSS
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    public Response get() {
        JsonObject themeConfig = getThemeConfig();
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("name", themeConfig.getString("name", "Teedy"));
        json.add("color", themeConfig.getString("color", "#ffffff"));
        json.add("css", themeConfig.getString("css", ""));
        return Response.ok().entity(json.build()).build();
    }

    /**
     * Change the theme configuration.
     *
     * @api {post} /theme Change the theme configuration
     * @apiName PostTheme
     * @apiGroup Theme
     * @apiParam {String} name Application name
     * @apiParam {String} color Main color
     * @apiParam {String} css Custom CSS
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission admin
     * @apiVersion 1.5.0
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

    /**
     * Change a theme image.
     *
     * @api {put} /theme/image/:type Change a theme image
     * @apiDescription This resource accepts only multipart/form-data.
     * @apiName PutThemeImage
     * @apiGroup Theme
     * @apiParam {String="logo","background"} type Image type
     * @apiParam {String} image Image data
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NoImageProvided An image is required
     * @apiError (server) CopyError Error copying the image to the theme directory
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param type Image type
     * @param imageBodyPart Image data
     * @return Response
     */
    @PUT
    @Path("image/{type: logo|background}")
    @Consumes("multipart/form-data")
    public Response images(@PathParam("type") String type,
            @FormDataParam("image") FormDataBodyPart imageBodyPart) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        if (imageBodyPart == null) {
            throw new ClientException("NoImageProvided", "An image is required");
        }

        // Only a background or a logo is handled
        java.nio.file.Path filePath = DirectoryUtil.getThemeDirectory().resolve(type);

        // Copy the image to the theme directory
        try (InputStream inputStream = imageBodyPart.getValueAs(InputStream.class)) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new ServerException("CopyError", "Error copying the image to the theme directory", e);
        }

        return Response.ok().build();
    }

    /**
     * Get theme images.
     *
     * @api {get} /theme/image/:type Get a theme image
     * @apiName GetThemeImage
     * @apiGroup Theme
     * @apiParam {String="logo","background"} type Image type
     * @apiSuccess {String} image The whole response is the image
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param type Image type
     * @return Response
     */
    @GET
    @Produces("image/*")
    @Path("image/{type: logo|background}")
    public Response getImage(@PathParam("type") final String type) {
        final java.nio.file.Path filePath = DirectoryUtil.getThemeDirectory().resolve(type);

        // Copy the image to the response output
        return Response.ok(new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                InputStream inputStream = null;
                try {
                    if (Files.exists(filePath)) {
                        inputStream = Files.newInputStream(filePath);
                    } else {
                        inputStream = getClass().getResource("/image/" + (type.equals("logo") ? "logo.png" : "background.jpg")).openStream();
                    }
                    ByteStreams.copy(inputStream, outputStream);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        outputStream.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        })
        .header(HttpHeaders.CONTENT_TYPE, "image/*")
        .header(HttpHeaders.CACHE_CONTROL, "public")
        .header(HttpHeaders.EXPIRES, HttpUtil.buildExpiresHeader(3_600_000L * 24L * 15L))
        .build();
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
