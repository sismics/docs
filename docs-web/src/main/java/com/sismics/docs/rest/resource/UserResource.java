package com.sismics.docs.rest.resource;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.Cookie;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.jpa.AuthenticationTokenDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.GroupDao;
import com.sismics.docs.core.dao.jpa.RoleBaseFunctionDao;
import com.sismics.docs.core.dao.jpa.UserDao;
import com.sismics.docs.core.dao.jpa.criteria.GroupCriteria;
import com.sismics.docs.core.dao.jpa.criteria.UserCriteria;
import com.sismics.docs.core.dao.jpa.dto.GroupDto;
import com.sismics.docs.core.dao.jpa.dto.UserDto;
import com.sismics.docs.core.event.DocumentDeletedAsyncEvent;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.AuthenticationToken;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.EncryptionUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.JsonUtil;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.security.UserPrincipal;
import com.sismics.util.filter.TokenBasedSecurityFilter;

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
     * @return Response
     */
    @PUT
    public Response register(
        @FormParam("username") String username,
        @FormParam("password") String password,
        @FormParam("email") String email,
        @FormParam("storage_quota") String storageQuotaStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate the input data
        username = ValidationUtil.validateLength(username, "username", 3, 50);
        ValidationUtil.validateAlphanumeric(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 3, 50);
        Long storageQuota = ValidationUtil.validateLong(storageQuotaStr, "storage_quota");
        ValidationUtil.validateEmail(email, "email");
        
        // Create the user
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setStorageQuota(storageQuota);
        user.setStorageCurrent(0l);
        try {
            user.setPrivateKey(EncryptionUtil.generatePrivateKey());
        } catch (NoSuchAlgorithmException e) {
            throw new ServerException("PrivateKeyError", "Error while generating a private key", e);
        }
        user.setCreateDate(new Date());
        
        // Create the user
        UserDao userDao = new UserDao();
        try {
            userDao.create(user, principal.getId());
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ServerException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown Server Error", e);
            }
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Updates user informations.
     * 
     * @param password Password
     * @param email E-Mail
     * @return Response
     */
    @POST
    public Response update(
        @FormParam("password") String password,
        @FormParam("email") String email) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate the input data
        password = ValidationUtil.validateLength(password, "password", 8, 50, true);
        email = ValidationUtil.validateLength(email, "email", null, 100, true);
        
        // Update the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(principal.getName());
        if (email != null) {
            user.setEmail(email);
        }
        user = userDao.update(user, principal.getId());
        
        // Change the password
        if (StringUtils.isNotBlank(password)) {
            user.setPassword(password);
            userDao.updatePassword(user, principal.getId());
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Updates user informations.
     * 
     * @param username Username
     * @param password Password
     * @param email E-Mail
     * @return Response
     */
    @POST
    @Path("{username: [a-zA-Z0-9_]+}")
    public Response update(
        @PathParam("username") String username,
        @FormParam("password") String password,
        @FormParam("email") String email,
        @FormParam("storage_quota") String storageQuotaStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate the input data
        password = ValidationUtil.validateLength(password, "password", 8, 50, true);
        email = ValidationUtil.validateLength(email, "email", null, 100, true);
        
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
        if (StringUtils.isNotBlank(storageQuotaStr)) {
            Long storageQuota = ValidationUtil.validateLong(storageQuotaStr, "storage_quota");
            user.setStorageQuota(storageQuota);
        }
        user = userDao.update(user, principal.getId());
        
        // Change the password
        if (StringUtils.isNotBlank(password)) {
            user.setPassword(password);
            userDao.updatePassword(user, principal.getId());
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Checks if a username is available. Search only on active accounts.
     * 
     * @param username Username to check
     * @return Response
     */
    @GET
    @Path("check_username")
    public Response checkUsername(
        @QueryParam("username") String username) {
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        
        JsonObjectBuilder response = Json.createObjectBuilder();
        if (user != null) {
            response.add("status", "ko")
                    .add("message", "Username already registered");
        } else {
            response.add("status", "ok");
        }
        
        return Response.ok().entity(response.build()).build();
    }

    /**
     * This resource is used to authenticate the user and create a user session.
     * The "session" is only used to identify the user, no other data is stored in the session.
     * 
     * @param username Username
     * @param password Password
     * @param longLasted Remember the user next time, create a long lasted session.
     * @return Response
     */
    @POST
    @Path("login")
    public Response login(
        @FormParam("username") String username,
        @FormParam("password") String password,
        @FormParam("remember") boolean longLasted) {
        // Validate the input data
        username = StringUtils.strip(username);
        password = StringUtils.strip(password);

        // Get the user
        UserDao userDao = new UserDao();
        String userId = userDao.authenticate(username, password);
        if (userId == null) {
            throw new ForbiddenClientException();
        }
        
        // Get the remote IP
        String ip = request.getHeader("x-forwarded-for");
        if (Strings.isNullOrEmpty(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Create a new session token
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = new AuthenticationToken();
        authenticationToken.setUserId(userId);
        authenticationToken.setLongLasted(longLasted);
        authenticationToken.setIp(ip);
        authenticationToken.setUserAgent(StringUtils.abbreviate(request.getHeader("user-agent"), 1000));
        String token = authenticationTokenDao.create(authenticationToken);
        
        // Cleanup old session tokens
        authenticationTokenDao.deleteOldSessionToken(userId);

        JsonObjectBuilder response = Json.createObjectBuilder();
        int maxAge = longLasted ? TokenBasedSecurityFilter.TOKEN_LONG_LIFETIME : -1;
        NewCookie cookie = new NewCookie(TokenBasedSecurityFilter.COOKIE_NAME, token, "/", null, null, maxAge, false);
        return Response.ok().entity(response.build()).cookie(cookie).build();
    }

    /**
     * Logs out the user and deletes the active session.
     * 
     * @return Response
     */
    @POST
    @Path("logout")
    public Response logout() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the value of the session token
        String authToken = getAuthToken();
        
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
        JsonObjectBuilder response = Json.createObjectBuilder();
        NewCookie cookie = new NewCookie(TokenBasedSecurityFilter.COOKIE_NAME, null);
        return Response.ok().entity(response.build()).cookie(cookie).build();
    }

    /**
     * Delete a user.
     * 
     * @return Response
     */
    @DELETE
    public Response delete() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Ensure that the admin user is not deleted
        if (hasBaseFunction(BaseFunction.ADMIN)) {
            throw new ClientException("ForbiddenError", "The admin user cannot be deleted");
        }
        
        // Find linked data
        DocumentDao documentDao = new DocumentDao();
        List<Document> documentList = documentDao.findByUserId(principal.getId());
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.findByUserId(principal.getId());
        
        // Delete the user
        UserDao userDao = new UserDao();
        userDao.delete(principal.getName(), principal.getId());
        
        // Raise deleted events for documents
        for (Document document : documentList) {
            DocumentDeletedAsyncEvent documentDeletedAsyncEvent = new DocumentDeletedAsyncEvent();
            documentDeletedAsyncEvent.setUserId(principal.getId());
            documentDeletedAsyncEvent.setDocumentId(document.getId());
            AppContext.getInstance().getAsyncEventBus().post(documentDeletedAsyncEvent);
        }
        
        // Raise deleted events for files (don't bother sending document updated event)
        for (File file : fileList) {
            FileDeletedAsyncEvent fileDeletedAsyncEvent = new FileDeletedAsyncEvent();
            fileDeletedAsyncEvent.setUserId(principal.getId());
            fileDeletedAsyncEvent.setFile(file);
            AppContext.getInstance().getAsyncEventBus().post(fileDeletedAsyncEvent);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Deletes a user.
     * 
     * @param username Username
     * @return Response
     */
    @DELETE
    @Path("{username: [a-zA-Z0-9_]+}")
    public Response delete(@PathParam("username") String username) {
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
        
        // Find linked data
        DocumentDao documentDao = new DocumentDao();
        List<Document> documentList = documentDao.findByUserId(user.getId());
        FileDao fileDao = new FileDao();
        List<File> fileList = fileDao.findByUserId(user.getId());
        
        // Delete the user
        userDao.delete(user.getUsername(), principal.getId());
        
        // Raise deleted events for documents
        for (Document document : documentList) {
            DocumentDeletedAsyncEvent documentDeletedAsyncEvent = new DocumentDeletedAsyncEvent();
            documentDeletedAsyncEvent.setUserId(principal.getId());
            documentDeletedAsyncEvent.setDocumentId(document.getId());
            AppContext.getInstance().getAsyncEventBus().post(documentDeletedAsyncEvent);
        }
        
        // Raise deleted events for files (don't bother sending document updated event)
        for (File file : fileList) {
            FileDeletedAsyncEvent fileDeletedAsyncEvent = new FileDeletedAsyncEvent();
            fileDeletedAsyncEvent.setUserId(principal.getId());
            fileDeletedAsyncEvent.setFile(file);
            AppContext.getInstance().getAsyncEventBus().post(fileDeletedAsyncEvent);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns the information about the connected user.
     * 
     * @return Response
     */
    @GET
    public Response info() {
        JsonObjectBuilder response = Json.createObjectBuilder();
        if (!authenticate()) {
            response.add("anonymous", true);

            // Check if admin has the default password
            UserDao userDao = new UserDao();
            User adminUser = userDao.getById("admin");
            if (adminUser != null && adminUser.getDeleteDate() == null) {
                response.add("is_default_password", Constants.DEFAULT_ADMIN_PASSWORD.equals(adminUser.getPassword()));
            }
        } else {
            // Update the last connection date
            String authToken = getAuthToken();
            AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
            authenticationTokenDao.updateLastConnectionDate(authToken);
            
            // Build the response
            response.add("anonymous", false);
            UserDao userDao = new UserDao();
            GroupDao groupDao = new GroupDao();
            User user = userDao.getById(principal.getId());
            List<GroupDto> groupDtoList = groupDao.findByCriteria(new GroupCriteria()
                    .setUserId(user.getId())
                    .setRecursive(true), null);
            
            response.add("username", user.getUsername())
                    .add("email", user.getEmail())
                    .add("storage_quota", user.getStorageQuota())
                    .add("storage_current", user.getStorageCurrent());
            
            // Base functions
            JsonArrayBuilder baseFunctions = Json.createArrayBuilder();
            for (String baseFunction : ((UserPrincipal) principal).getBaseFunctionSet()) {
                baseFunctions.add(baseFunction);
            }
            
            // Groups
            JsonArrayBuilder groups = Json.createArrayBuilder();
            for (GroupDto groupDto : groupDtoList) {
                groups.add(groupDto.getName());
            }
            
            response.add("base_functions", baseFunctions)
                    .add("groups", groups)
                    .add("is_default_password", hasBaseFunction(BaseFunction.ADMIN) && Constants.DEFAULT_ADMIN_PASSWORD.equals(user.getPassword()));
        }
        
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns the information about a user.
     * 
     * @param username Username
     * @return Response
     */
    @GET
    @Path("{username: [a-zA-Z0-9_]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("username") String username) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user doesn't exist");
        }
        
        // Groups
        GroupDao groupDao = new GroupDao();
        List<GroupDto> groupDtoList = groupDao.findByCriteria(
                new GroupCriteria().setUserId(user.getId()),
                new SortCriteria(1, true));
        JsonArrayBuilder groups = Json.createArrayBuilder();
        for (GroupDto groupDto : groupDtoList) {
            groups.add(groupDto.getName());
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("username", user.getUsername())
                .add("groups", groups)
                .add("email", user.getEmail())
                .add("storage_quota", user.getStorageQuota())
                .add("storage_current", user.getStorageCurrent());
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all active users.
     * 
     * @param limit Page limit
     * @param offset Page offset
     * @param sortColumn Sort index
     * @param asc If true, ascending sorting, else descending
     * @return Response
     */
    @GET
    @Path("list")
    public Response list(
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        JsonArrayBuilder users = Json.createArrayBuilder();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        UserDao userDao = new UserDao();
        List<UserDto> userDtoList = userDao.findByCriteria(new UserCriteria(), sortCriteria);
        for (UserDto userDto : userDtoList) {
            users.add(Json.createObjectBuilder()
                    .add("id", userDto.getId())
                    .add("username", userDto.getUsername())
                    .add("email", userDto.getEmail())
                    .add("storage_quota", userDto.getStorageQuota())
                    .add("storage_current", userDto.getStorageCurrent())
                    .add("create_date", userDto.getCreateTimestamp()));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("users", users);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all active sessions.
     * 
     * @return Response
     */
    @GET
    @Path("session")
    public Response session() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the value of the session token
        String authToken = getAuthToken();
        
        JsonArrayBuilder sessions = Json.createArrayBuilder();
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();

        for (AuthenticationToken authenticationToken : authenticationTokenDao.getByUserId(principal.getId())) {
            JsonObjectBuilder session = Json.createObjectBuilder()
                    .add("create_date", authenticationToken.getCreationDate().getTime())
                    .add("ip", JsonUtil.nullable(authenticationToken.getIp()))
                    .add("user_agent", JsonUtil.nullable(authenticationToken.getUserAgent()));
            if (authenticationToken.getLastConnectionDate() != null) {
                session.add("last_connection_date", authenticationToken.getLastConnectionDate().getTime());
            }
            session.add("current", authenticationToken.getId().equals(authToken));
            sessions.add(session);
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("sessions", sessions);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Deletes all active sessions except the one used for this request.
     * 
     * @return Response
     */
    @DELETE
    @Path("session")
    public Response deleteSession() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the value of the session token
        String authToken = getAuthToken();
        
        // Remove other tokens
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        authenticationTokenDao.deleteByUserId(principal.getId(), authToken);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns the authentication token value.
     * 
     * @return Token value
     */
    private String getAuthToken() {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
