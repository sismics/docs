package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.jpa.AuthenticationTokenDao;
import com.sismics.docs.core.dao.jpa.RoleBaseFunctionDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.jpa.criteria.UserCriteria;
import com.sismics.docs.core.dao.jpa.dto.UserDto;
import com.sismics.docs.core.model.jpa.AuthenticationToken;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.security.UserPrincipal;
import com.sismics.util.LocaleUtil;
import com.sismics.util.filter.TokenBasedSecurityFilter;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * User REST resources.
 * 
 * @author jtremeaux
 */
@Path("/user")
public class UserResource extends BaseResource {
    /**
     * Creates a new user.
     * 
     * @param username User's username
     * @param password Password
     * @param email E-Mail
     * @param localeId Locale ID
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(
        @FormParam("username") String username,
        @FormParam("password") String password,
        @FormParam("locale") String localeId,
        @FormParam("email") String email) throws JSONException {

        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate the input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateAlphanumeric(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 3, 50);
        ValidationUtil.validateEmail(email, "email");
        
        // Create the user
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        try {
            user.setPrivateKey(EncryptionUtil.generatePrivateKey());
        } catch (NoSuchAlgorithmException e) {
            throw new ServerException("PrivateKeyError", "Error while generating a private key", e);
        }
        user.setCreateDate(new Date());

        if (localeId == null) {
            // Set the locale from the HTTP headers
            localeId = LocaleUtil.getLocaleIdFromAcceptLanguage(request.getHeader("Accept-Language"));
        }
        user.setLocaleId(localeId);
        
        // Create the user
        UserDao userDao = new UserDao();
        try {
            userDao.create(user);
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ServerException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown Server Error", e);
            }
        }
        
        // Always return OK
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Updates user informations.
     * 
     * @param password Password
     * @param email E-Mail
     * @param themeId Theme
     * @param localeId Locale ID
     * @param firstConnection True if the user hasn't acknowledged the first connection wizard yet.
     * @return Response
     * @throws JSONException
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @FormParam("password") String password,
        @FormParam("email") String email,
        @FormParam("theme") String themeId,
        @FormParam("locale") String localeId,
        @FormParam("first_connection") Boolean firstConnection) throws JSONException {
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate the input data
        password = ValidationUtil.validateLength(password, "password", 8, 50, true);
        email = ValidationUtil.validateLength(email, "email", null, 100, true);
        localeId = ValidationUtil.validateLocale(localeId, "locale", true);
        themeId = ValidationUtil.validateTheme(themeId, "theme", true);
        
        // Update the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(principal.getName());
        if (email != null) {
            user.setEmail(email);
        }
        if (themeId != null) {
            user.setTheme(themeId);
        }
        if (localeId != null) {
            user.setLocaleId(localeId);
        }
        if (firstConnection != null && hasBaseFunction(BaseFunction.ADMIN)) {
            user.setFirstConnection(firstConnection);
        }
        
        user = userDao.update(user);
        
        if (StringUtils.isNotBlank(password)) {
            user.setPassword(password);
            userDao.updatePassword(user);
        }
        
        // Always return "ok"
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Updates user informations.
     * 
     * @param username Username
     * @param password Password
     * @param email E-Mail
     * @param themeId Theme
     * @param localeId Locale ID
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{username: [a-zA-Z0-9_]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
        @PathParam("username") String username,
        @FormParam("password") String password,
        @FormParam("email") String email,
        @FormParam("theme") String themeId,
        @FormParam("locale") String localeId) throws JSONException {
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate the input data
        password = ValidationUtil.validateLength(password, "password", 8, 50, true);
        email = ValidationUtil.validateLength(email, "email", null, 100, true);
        localeId = ValidationUtil.validateLocale(localeId, "locale", true);
        themeId = ValidationUtil.validateTheme(themeId, "theme", true);
        
        // Check if the user exists
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }

        // Update the user
        if (email != null) {
            user.setEmail(email);
        }
        if (themeId != null) {
            user.setTheme(themeId);
        }
        if (localeId != null) {
            user.setLocaleId(localeId);
        }
        
        user = userDao.update(user);
        
        if (StringUtils.isNotBlank(password)) {
            // Change the password
            user.setPassword(password);
            userDao.updatePassword(user);
        }
        
        // Always return "ok"
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }

    /**
     * Checks if a username is available. Search only on active accounts.
     * 
     * @param username Username to check
     * @return Response
     */
    @GET
    @Path("check_username")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkUsername(
        @QueryParam("username") String username) throws JSONException {
        
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        
        JSONObject response = new JSONObject();
        if (user != null) {
            response.put("status", "ko");
            response.put("message", "Username already registered");
        } else {
            response.put("status", "ok");
        }
        
        return Response.ok().entity(response).build();
    }

    /**
     * This resource is used to authenticate the user and create a user ession.
     * The "session" is only used to identify the user, no other data is stored in the session.
     * 
     * @param username Username
     * @param password Password
     * @param longLasted Remember the user next time, create a long lasted session.
     * @return Response
     */
    @POST
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
        @FormParam("username") String username,
        @FormParam("password") String password,
        @FormParam("remember") boolean longLasted) throws JSONException {
        
        // Validate the input data
        username = StringUtils.strip(username);
        password = StringUtils.strip(password);

        // Get the user
        UserDao userDao = new UserDao();
        String userId = userDao.authenticate(username, password);
        if (userId == null) {
            throw new ForbiddenClientException();
        }
            
        // Create a new session token
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = new AuthenticationToken();
        authenticationToken.setUserId(userId);
        authenticationToken.setLongLasted(longLasted);
        String token = authenticationTokenDao.create(authenticationToken);
        
        // Cleanup old session tokens
        authenticationTokenDao.deleteOldSessionToken(userId);

        JSONObject response = new JSONObject();
        int maxAge = longLasted ? TokenBasedSecurityFilter.TOKEN_LONG_LIFETIME : -1;
        NewCookie cookie = new NewCookie(TokenBasedSecurityFilter.COOKIE_NAME, token, "/", null, null, maxAge, false);
        return Response.ok().entity(response).cookie(cookie).build();
    }

    /**
     * Logs out the user and deletes the active session.
     * 
     * @return Response
     */
    @POST
    @Path("logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the value of the session token
        String authToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                    authToken = cookie.getValue();
                }
            }
        }
        
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = null;
        if (authToken != null) {
            authenticationToken = authenticationTokenDao.get(authToken);
        }
        
        // No token : nothing to do
        if (authenticationToken == null) {
            throw new ForbiddenClientException();
        }
        
        // Deletes the server token
        try {
            authenticationTokenDao.delete(authToken);
        } catch (Exception e) {
            throw new ServerException("AuthenticationTokenError", "Error deleting authentication token: " + authToken, e);
        }
        
        // Deletes the client token in the HTTP response
        JSONObject response = new JSONObject();
        NewCookie cookie = new NewCookie(TokenBasedSecurityFilter.COOKIE_NAME, null);
        return Response.ok().entity(response).cookie(cookie).build();
    }

    /**
     * Delete a user.
     * 
     * @return Response
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Ensure that the admin user is not deleted
        if (hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ClientException("ForbiddenError", "The admin user cannot be deleted");
        }
        
        // Delete the user
        UserDao userDao = new UserDao();
        userDao.delete(principal.getName());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Deletes a user.
     * 
     * @param username Username
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{username: [a-zA-Z0-9_]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("username") String username) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Check if the user exists
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }
        
        // Ensure that the admin user is not deleted
        RoleBaseFunctionDao userBaseFuction = new RoleBaseFunctionDao();
        Set<String> baseFunctionSet = userBaseFuction.findByRoleId(user.getRoleId());
        if (baseFunctionSet.contains(BaseFunction.ADMIN.name())) {
            throw new ClientException("ForbiddenError", "The admin user cannot be deleted");
        }
        
        // Delete the user
        userDao.delete(user.getUsername());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns the information about the connected user.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response info() throws JSONException {
        JSONObject response = new JSONObject();
        if (!authenticate()) {
            response.put("anonymous", true);

            String localeId = LocaleUtil.getLocaleIdFromAcceptLanguage(request.getHeader("Accept-Language"));
            response.put("locale", localeId);
            
            // Check if admin has the default password
            UserDao userDao = new UserDao();
            User adminUser = userDao.getById("admin");
            if (adminUser != null && adminUser.getDeleteDate() == null) {
                response.put("is_default_password", Constants.DEFAULT_ADMIN_PASSWORD.equals(adminUser.getPassword()));
            }
        } else {
            response.put("anonymous", false);
            UserDao userDao = new UserDao();
            User user = userDao.getById(principal.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("theme", user.getTheme());
            response.put("locale", user.getLocaleId());
            response.put("first_connection", user.isFirstConnection());
            JSONArray baseFunctions = new JSONArray(((UserPrincipal) principal).getBaseFunctionSet());
            response.put("base_functions", baseFunctions);
            response.put("is_default_password", hasBaseFunction(BaseFunction.ADMIN) && Constants.DEFAULT_ADMIN_PASSWORD.equals(user.getPassword()));
        }
        
        return Response.ok().entity(response).build();
    }

    /**
     * Returns the information about a user.
     * 
     * @param username Username
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{username: [a-zA-Z0-9_]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("username") String username) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        JSONObject response = new JSONObject();
        
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }
        
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("theme", user.getTheme());
        response.put("locale", user.getLocaleId());
        
        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns all active users.
     * 
     * @param limit Page limit
     * @param offset Page offset
     * @param sortColumn Sort index
     * @param asc If true, ascending sorting, else descending
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        JSONObject response = new JSONObject();
        List<JSONObject> users = new ArrayList<>();
        
        PaginatedList<UserDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        UserDao userDao = new UserDao();
        userDao.findByCriteria(paginatedList, new UserCriteria(), sortCriteria);
        for (UserDto userDto : paginatedList.getResultList()) {
            JSONObject user = new JSONObject();
            user.put("id", userDto.getId());
            user.put("username", userDto.getUsername());
            user.put("email", userDto.getEmail());
            user.put("create_date", userDto.getCreateTimestamp());
            users.add(user);
        }
        response.put("total", paginatedList.getResultCount());
        response.put("users", users);
        
        return Response.ok().entity(response).build();
    }
    
    /**
     * Returns all active sessions.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response session() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the value of the session token
        String authToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                    authToken = cookie.getValue();
                }
            }
        }
        
        JSONObject response = new JSONObject();
        List<JSONObject> sessions = new ArrayList<>();
        
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();

        for (AuthenticationToken authenticationToken : authenticationTokenDao.getByUserId(principal.getId())) {
            JSONObject session = new JSONObject();
            session.put("create_date", authenticationToken.getCreationDate().getTime());
            if (authenticationToken.getLastConnectionDate() != null) {
                session.put("last_connection_date", authenticationToken.getLastConnectionDate().getTime());
            }
            session.put("current", authenticationToken.getId().equals(authToken));
            sessions.add(session);
        }
        response.put("sessions", sessions);
        
        return Response.ok().entity(response).build();
    }
    
    /**
     * Deletes all active sessions except the one used for this request.
     * 
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("session")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSession() throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the value of the session token
        String authToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                    authToken = cookie.getValue();
                }
            }
        }
        
        // Remove other tokens
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        authenticationTokenDao.deleteByUserId(principal.getId(), authToken);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
