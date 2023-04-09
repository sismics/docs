package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.sismics.docs.core.constant.AclTargetType;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.*;
import com.sismics.docs.core.dao.criteria.GroupCriteria;
import com.sismics.docs.core.dao.criteria.UserCriteria;
import com.sismics.docs.core.dao.dto.GroupDto;
import com.sismics.docs.core.dao.dto.UserDto;
import com.sismics.docs.core.event.DocumentDeletedAsyncEvent;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.event.PasswordLostEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.*;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.docs.core.util.RoutingUtil;
import com.sismics.docs.core.util.authentication.AuthenticationUtil;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.security.UserPrincipal;
import com.sismics.util.JsonUtil;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import com.sismics.util.totp.GoogleAuthenticator;
import com.sismics.util.totp.GoogleAuthenticatorKey;
import org.apache.commons.lang3.StringUtils;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.Cookie;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
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
     * @api {put} /user Register a new user
     * @apiName PutUser
     * @apiGroup User
     * @apiParam {String{3..50}} username Username
     * @apiParam {String{8..50}} password Password
     * @apiParam {String{1..100}} email E-mail
     * @apiParam {Number} storage_quota Storage quota (in bytes)
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (server) PrivateKeyError Error while generating a private key
     * @apiError (client) AlreadyExistingUsername Login already used
     * @apiError (server) UnknownError Unknown server error
     * @apiPermission admin
     * @apiVersion 1.5.0
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
        ValidationUtil.validateUsername(username, "username");
        password = ValidationUtil.validateLength(password, "password", 8, 50);
        email = ValidationUtil.validateLength(email, "email", 1, 100);
        Long storageQuota = ValidationUtil.validateLong(storageQuotaStr, "storage_quota");
        ValidationUtil.validateEmail(email, "email");
        
        // Create the user
        User user = new User();
        user.setRoleId(Constants.DEFAULT_USER_ROLE);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setStorageQuota(storageQuota);
        user.setOnboarding(true);

        // Create the user
        UserDao userDao = new UserDao();
        try {
            userDao.create(user, principal.getId());
        } catch (Exception e) {
            if ("AlreadyExistingUsername".equals(e.getMessage())) {
                throw new ClientException("AlreadyExistingUsername", "Login already used", e);
            } else {
                throw new ServerException("UnknownError", "Unknown server error", e);
            }
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Updates the current user informations.
     *
     * @api {post} /user Update the current user
     * @apiName PostUser
     * @apiGroup User
     * @apiParam {String{8..50}} password Password
     * @apiParam {String{1..100}} email E-mail
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied or connected as guest
     * @apiError (client) ValidationError Validation error
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param password Password
     * @param email E-Mail
     * @return Response
     */
    @POST
    public Response update(
        @FormParam("password") String password,
        @FormParam("email") String email) {
        if (!authenticate() || principal.isGuest()) {
            throw new ForbiddenClientException();
        }
        
        // Validate the input data
        password = ValidationUtil.validateLength(password, "password", 8, 50, true);
        email = ValidationUtil.validateLength(email, "email", 1, 100, true);
        
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
     * Updates a user informations.
     *
     * @api {post} /user/:username Update a user
     * @apiName PostUserUsername
     * @apiGroup User
     * @apiParam {String} username Username
     * @apiParam {String{8..50}} password Password
     * @apiParam {String{1..100}} email E-mail
     * @apiParam {Number} storage_quota Storage quota (in bytes)
     * @apiParam {Boolean} disabled Disabled status
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) UserNotFound User not found
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param username Username
     * @param password Password
     * @param email E-Mail
     * @return Response
     */
    @POST
    @Path("{username: [a-zA-Z0-9_@\\.]+}")
    public Response update(
        @PathParam("username") String username,
        @FormParam("password") String password,
        @FormParam("email") String email,
        @FormParam("storage_quota") String storageQuotaStr,
        @FormParam("disabled") Boolean disabled) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate the input data
        password = ValidationUtil.validateLength(password, "password", 8, 50, true);
        email = ValidationUtil.validateLength(email, "email", 1, 100, true);
        
        // Check if the user exists
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user does not exist");
        }

        // Update the user
        if (email != null) {
            user.setEmail(email);
        }
        if (StringUtils.isNotBlank(storageQuotaStr)) {
            Long storageQuota = ValidationUtil.validateLong(storageQuotaStr, "storage_quota");
            user.setStorageQuota(storageQuota);
        }
        if (disabled != null) {
            // Cannot disable the admin user or the guest user
            RoleBaseFunctionDao userBaseFuction = new RoleBaseFunctionDao();
            Set<String> baseFunctionSet = userBaseFuction.findByRoleId(Sets.newHashSet(user.getRoleId()));
            if (Constants.GUEST_USER_ID.equals(username) || baseFunctionSet.contains(BaseFunction.ADMIN.name())) {
                disabled = false;
            }

            if (disabled && user.getDisableDate() == null) {
                // Recording the disabled date
                user.setDisableDate(new Date());
            } else if (!disabled && user.getDisableDate() != null) {
                // Emptying the disabled date
                user.setDisableDate(null);
            }
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
     * Checks if a username is available.
     * Search only on active accounts.
     *
     * @api {get} /user/check_username Check username availability
     * @apiName GetUserCheckUsername
     * @apiGroup User
     * @apiParam {String} username Username
     * @apiSuccess {String} status Status OK or KO
     * @apiPermission none
     * @apiVersion 1.5.0
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
     * @api {post} /user/login Login a user
     * @apiDescription This resource creates an authentication token and gives it back in a cookie.
     * All authenticated resources will check this cookie to find the user currently logged in.
     * @apiName PostUserLogin
     * @apiGroup User
     * @apiParam {String} username Username
     * @apiParam {String} password Password (optional for guest login)
     * @apiParam {String} code TOTP validation code
     * @apiParam {Boolean} remember If true, create a long lasted token
     * @apiSuccess {String} auth_token A cookie named auth_token containing the token ID
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationCodeRequired A TOTP validation code is required
     * @apiPermission none
     * @apiVersion 1.5.0
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
        @FormParam("code") String validationCodeStr,
        @FormParam("remember") boolean longLasted) {
        // Validate the input data
        username = StringUtils.strip(username);
        password = StringUtils.strip(password);

        // Get the user
        UserDao userDao = new UserDao();
        User user = null;
        if (Constants.GUEST_USER_ID.equals(username)) {
            if (ConfigUtil.getConfigBooleanValue(ConfigType.GUEST_LOGIN)) {
                // Login as guest
                user = userDao.getActiveByUsername(Constants.GUEST_USER_ID);
            }
        } else {
            // Login as a normal user
            user = AuthenticationUtil.authenticate(username, password);
        }
        if (user == null) {
            throw new ForbiddenClientException();
        }

        // Two factor authentication
        if (user.getTotpKey() != null) {
            // If TOTP is enabled, ask a validation code
            if (Strings.isNullOrEmpty(validationCodeStr)) {
                throw new ClientException("ValidationCodeRequired", "An OTP validation code is required");
            }
            
            // Check the validation code
            int validationCode = ValidationUtil.validateInteger(validationCodeStr, "code");
            GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
            if (!googleAuthenticator.authorize(user.getTotpKey(), validationCode)) {
                throw new ForbiddenClientException();
            }
        }
        
        // Get the remote IP
        String ip = request.getHeader("x-forwarded-for");
        if (Strings.isNullOrEmpty(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Create a new session token
        AuthenticationTokenDao authenticationTokenDao = new AuthenticationTokenDao();
        AuthenticationToken authenticationToken = new AuthenticationToken()
            .setUserId(user.getId())
            .setLongLasted(longLasted)
            .setIp(StringUtils.abbreviate(ip, 45))
            .setUserAgent(StringUtils.abbreviate(request.getHeader("user-agent"), 1000));
        String token = authenticationTokenDao.create(authenticationToken);
        
        // Cleanup old session tokens
        authenticationTokenDao.deleteOldSessionToken(user.getId());

        JsonObjectBuilder response = Json.createObjectBuilder();
        int maxAge = longLasted ? TokenBasedSecurityFilter.TOKEN_LONG_LIFETIME : -1;
        NewCookie cookie = new NewCookie(TokenBasedSecurityFilter.COOKIE_NAME, token, "/", null, null, maxAge, false);
        return Response.ok().entity(response.build()).cookie(cookie).build();
    }

    /**
     * Logs out the user and deletes the active session.
     *
     * @api {post} /user/logout Logout a user
     * @apiDescription This resource deletes the authentication token created by POST /user/login and removes the cookie.
     * @apiName PostUserLogout
     * @apiGroup User
     * @apiSuccess {String} auth_token An expired cookie named auth_token containing no value
     * @apiError (client) ForbiddenError Access denied
     * @apiError (server) AuthenticationTokenError Error deleting the authentication token
     * @apiPermission user
     * @apiVersion 1.5.0
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
            throw new ServerException("AuthenticationTokenError", "Error deleting the authentication token: " + authToken, e);
        }
        
        // Deletes the client token in the HTTP response
        JsonObjectBuilder response = Json.createObjectBuilder();
        NewCookie cookie = new NewCookie(TokenBasedSecurityFilter.COOKIE_NAME, null, "/", null, 1, null, -1, new Date(1), false, false);
        return Response.ok().entity(response.build()).cookie(cookie).build();
    }

    /**
     * Deletes the current user.
     *
     * @api {delete} /user Delete the current user
     * @apiDescription All associated entities will be deleted as well.
     * @apiName DeleteUser
     * @apiGroup User
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied or the user cannot be deleted
     * @apiError (client) UserUsedInRouteModel The user is used in a route model
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @DELETE
    public Response delete() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Ensure that the admin or guest users are not deleted
        if (hasBaseFunction(BaseFunction.ADMIN) || principal.isGuest()) {
            throw new ClientException("ForbiddenError", "This user cannot be deleted");
        }

        // Check that this user is not used in any workflow
        String routeModelName = RoutingUtil.findRouteModelNameByTargetName(AclTargetType.USER, principal.getName());
        if (routeModelName != null) {
            throw new ClientException("UserUsedInRouteModel", routeModelName);
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
            ThreadLocalContext.get().addAsyncEvent(documentDeletedAsyncEvent);
        }
        
        // Raise deleted events for files (don't bother sending document updated event)
        for (File file : fileList) {
            FileDeletedAsyncEvent fileDeletedAsyncEvent = new FileDeletedAsyncEvent();
            fileDeletedAsyncEvent.setUserId(principal.getId());
            fileDeletedAsyncEvent.setFileId(file.getId());
            ThreadLocalContext.get().addAsyncEvent(fileDeletedAsyncEvent);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Deletes a user.
     *
     * @api {delete} /user/:username Delete a user
     * @apiDescription All associated entities will be deleted as well.
     * @apiName DeleteUserUsername
     * @apiGroup User
     * @apiParam {String} username Username
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied or the user cannot be deleted
     * @apiError (client) UserNotFound The user does not exist
     * @apiError (client) UserUsedInRouteModel The user is used in a route model
     * @apiPermission admin
     * @apiVersion 1.5.0
     *
     * @param username Username
     * @return Response
     */
    @DELETE
    @Path("{username: [a-zA-Z0-9_@\\.]+}")
    public Response delete(@PathParam("username") String username) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Cannot delete the guest user
        if (Constants.GUEST_USER_ID.equals(username)) {
            throw new ClientException("ForbiddenError", "The guest user cannot be deleted");
        }

        // Check that the user exists
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user does not exist");
        }
        
        // Ensure that the admin user is not deleted
        RoleBaseFunctionDao roleBaseFunctionDao = new RoleBaseFunctionDao();
        Set<String> baseFunctionSet = roleBaseFunctionDao.findByRoleId(Sets.newHashSet(user.getRoleId()));
        if (baseFunctionSet.contains(BaseFunction.ADMIN.name())) {
            throw new ClientException("ForbiddenError", "The admin user cannot be deleted");
        }

        // Check that this user is not used in any workflow
        String routeModelName = RoutingUtil.findRouteModelNameByTargetName(AclTargetType.USER, username);
        if (routeModelName != null) {
            throw new ClientException("UserUsedInRouteModel", routeModelName);
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
            ThreadLocalContext.get().addAsyncEvent(documentDeletedAsyncEvent);
        }
        
        // Raise deleted events for files (don't bother sending document updated event)
        for (File file : fileList) {
            FileDeletedAsyncEvent fileDeletedAsyncEvent = new FileDeletedAsyncEvent();
            fileDeletedAsyncEvent.setUserId(principal.getId());
            fileDeletedAsyncEvent.setFileId(file.getId());
            ThreadLocalContext.get().addAsyncEvent(fileDeletedAsyncEvent);
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Disable time-based one-time password for a specific user.
     *
     * @api {post} /user/:username/disable_totp Disable TOTP authentication for a specific user
     * @apiName PostUserUsernameDisableTotp
     * @apiGroup User
     * @apiParam {String} username Username
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied or connected as guest
     * @apiError (client) ValidationError Validation error
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param username Username
     * @return Response
     */
    @POST
    @Path("{username: [a-zA-Z0-9_@\\.]+}/disable_totp")
    public Response disableTotpUsername(@PathParam("username") String username) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Get the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ForbiddenClientException();
        }

        // Remove the TOTP key
        user.setTotpKey(null);
        userDao.update(user, principal.getId());

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns the information about the connected user.
     *
     * @api {get} /user Get the current user
     * @apiName GetUser
     * @apiGroup User
     * @apiSuccess {Boolean} anonymous True if no user is connected
     * @apiSuccess {Boolean} is_default_password True if the admin has the default password
     * @apiSuccess {Boolean} onboarding True if the UI needs to display the onboarding
     * @apiSuccess {String} username Username
     * @apiSuccess {String} email E-mail
     * @apiSuccess {Number} storage_quota Storage quota (in bytes)
     * @apiSuccess {Number} storage_current Quota used (in bytes)
     * @apiSuccess {Boolean} totp_enabled True if TOTP authentication is enabled
     * @apiSuccess {String[]} base_functions Base functions
     * @apiSuccess {String[]} groups Groups
     * @apiPermission none
     * @apiVersion 1.5.0
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
                    .add("storage_current", user.getStorageCurrent())
                    .add("totp_enabled", user.getTotpKey() != null)
                    .add("onboarding", user.isOnboarding());

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
     * @api {get} /user/:username Get a user
     * @apiName GetUserUsername
     * @apiGroup User
     * @apiParam {String} username Username
     * @apiSuccess {String} username Username
     * @apiSuccess {String} email E-mail
     * @apiSuccess {Boolean} totp_enabled True if TOTP authentication is enabled
     * @apiSuccess {Number} storage_quota Storage quota (in bytes)
     * @apiSuccess {Number} storage_current Quota used (in bytes)
     * @apiSuccess {String[]} groups Groups
     * @apiSuccess {Boolean} disabled True if the user is disabled
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) UserNotFound The user does not exist
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param username Username
     * @return Response
     */
    @GET
    @Path("{username: [a-zA-Z0-9_@\\.]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response view(@PathParam("username") String username) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(username);
        if (user == null) {
            throw new ClientException("UserNotFound", "The user does not exist");
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
                .add("totp_enabled", user.getTotpKey() != null)
                .add("storage_quota", user.getStorageQuota())
                .add("storage_current", user.getStorageCurrent())
                .add("disabled", user.getDisableDate() != null);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns all active users.
     *
     * @api {get} /user/list Get users
     * @apiName GetUserList
     * @apiGroup User
     * @apiParam {Number} sort_column Column index to sort on
     * @apiParam {Boolean} asc If true, sort in ascending order
     * @apiParam {String} group Filter on this group
     * @apiSuccess {Object[]} users List of users
     * @apiSuccess {String} users.id ID
     * @apiSuccess {String} users.username Username
     * @apiSuccess {String} users.email E-mail
     * @apiSuccess {Boolean} users.totp_enabled True if TOTP authentication is enabled
     * @apiSuccess {Number} users.storage_quota Storage quota (in bytes)
     * @apiSuccess {Number} users.storage_current Quota used (in bytes)
     * @apiSuccess {Number} users.create_date Create date (timestamp)
     * @apiSuccess {Number} users.disabled True if the user is disabled
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param sortColumn Sort index
     * @param asc If true, ascending sorting, else descending
     * @param groupName Only return users from this group
     * @return Response
     */
    @GET
    @Path("list")
    public Response list(
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("group") String groupName) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        JsonArrayBuilder users = Json.createArrayBuilder();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        // Validate the group
        String groupId = null;
        if (!Strings.isNullOrEmpty(groupName)) {
            GroupDao groupDao = new GroupDao();
            Group group = groupDao.getActiveByName(groupName);
            if (group != null) {
                groupId = group.getId();
            }
        }
        
        UserDao userDao = new UserDao();
        List<UserDto> userDtoList = userDao.findByCriteria(new UserCriteria().setGroupId(groupId), sortCriteria);
        for (UserDto userDto : userDtoList) {
            users.add(Json.createObjectBuilder()
                    .add("id", userDto.getId())
                    .add("username", userDto.getUsername())
                    .add("email", userDto.getEmail())
                    .add("totp_enabled", userDto.getTotpKey() != null)
                    .add("storage_quota", userDto.getStorageQuota())
                    .add("storage_current", userDto.getStorageCurrent())
                    .add("create_date", userDto.getCreateTimestamp())
                    .add("disabled", userDto.getDisableTimestamp() != null));
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("users", users);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Returns all active sessions.
     *
     * @api {get} /user/session Get active sessions
     * @apiDescription This resource lists all active token which can be used to log in to the current user account.
     * @apiName GetUserSession
     * @apiGroup User
     * @apiSuccess {Object[]} sessions List of sessions
     * @apiSuccess {Number} create_date Create date of this token
     * @apiSuccess {String} ip IP used to log in
     * @apiSuccess {String} user_agent User agent used to log in
     * @apiSuccess {Number} last_connection_date Last connection date (timestamp)
     * @apiSuccess {Boolean} current If true, this token is the current one
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.5.0
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

        // The guest user cannot see other sessions
        if (!principal.isGuest()) {
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
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("sessions", sessions);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Deletes all active sessions except the one used for this request.
     *
     * @api {delete} /user/session Delete all sessions
     * @apiDescription This resource deletes all active token linked to this account, except the one used to make this request.
     * @apiName DeleteUserSession
     * @apiGroup User
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied or connected as guest
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @DELETE
    @Path("session")
    public Response deleteSession() {
        if (!authenticate() || principal.isGuest()) {
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
     * Mark the onboarding experience as passed.
     *
     * @api {post} /user/onboarded Mark the onboarding experience as passed
     * @apiDescription Once the onboarding experience has been passed by the user, this resource prevent it from being displayed again.
     * @apiName PostUserOnboarded
     * @apiGroup User
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiPermission user
     * @apiVersion 1.7.0
     *
     * @return Response
     */
    @POST
    @Path("onboarded")
    public Response onboarded() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Save it
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(principal.getName());
        user.setOnboarding(false);
        userDao.updateOnboarding(user);

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Enable time-based one-time password.
     *
     * @api {post} /user/enable_totp Enable TOTP authentication
     * @apiDescription This resource enables the Time-based One-time Password authentication.
     * All following login will need a validation code generated from the given secret seed.
     * @apiName PostUserEnableTotp
     * @apiGroup User
     * @apiSuccess {String} secret Secret TOTP seed to initiate the algorithm
     * @apiError (client) ForbiddenError Access denied or connected as guest
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @POST
    @Path("enable_totp")
    public Response enableTotp() {
        if (!authenticate() || principal.isGuest()) {
            throw new ForbiddenClientException();
        }
        
        // Create a new TOTP key
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        
        // Save it
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(principal.getName());
        user.setTotpKey(key.getKey());
        userDao.update(user, principal.getId());
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("secret", key.getKey());
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Test time-based one-time password.
     *
     * @api {post} /user/test_totp Test TOTP authentication
     * @apiDescription Test a TOTP validation code.
     * @apiName PostUserTestTotp
     * @apiParam {String} code TOTP validation code
     * @apiGroup User
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError The validation code is not valid or access denied
     * @apiPermission user
     * @apiVersion 1.6.0
     *
     * @return Response
     */
    @POST
    @Path("test_totp")
    public Response testTotp(@FormParam("code") String validationCodeStr) {
        if (!authenticate() || principal.isGuest()) {
            throw new ForbiddenClientException();
        }

        // Get the user
        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(principal.getName());

        // Test the validation code
        if (user.getTotpKey() != null) {
            int validationCode = ValidationUtil.validateInteger(validationCodeStr, "code");
            GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
            if (!googleAuthenticator.authorize(user.getTotpKey(), validationCode)) {
                throw new ForbiddenClientException();
            }
        }

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Disable time-based one-time password for the current user.
     *
     * @api {post} /user/disable_totp Disable TOTP authentication for the current user
     * @apiName PostUserDisableTotp
     * @apiGroup User
     * @apiParam {String{1..100}} password Password
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied or connected as guest
     * @apiError (client) ValidationError Validation error
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param password Password
     * @return Response
     */
    @POST
    @Path("disable_totp")
    public Response disableTotp(@FormParam("password") String password) {
        if (!authenticate() || principal.isGuest()) {
            throw new ForbiddenClientException();
        }
        
        // Validate the input data
        password = ValidationUtil.validateLength(password, "password", 1, 100, false);

        // Check the password and get the user
        UserDao userDao = new UserDao();
        User user = userDao.authenticate(principal.getName(), password);
        if (user == null) {
            throw new ForbiddenClientException();
        }
        
        // Remove the TOTP key
        user.setTotpKey(null);
        userDao.update(user, principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Create a key to reset a password and send it by email.
     *
     * @api {post} /user/password_lost Create a key to reset a password and send it by email
     * @apiName PostUserPasswordLost
     * @apiGroup User
     * @apiParam {String} username Username
     * @apiSuccess {String} status Status OK
     * @apiError (client) ValidationError Validation error
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param username Username
     * @return Response
     */
    @POST
    @Path("password_lost")
    @Produces(MediaType.APPLICATION_JSON)
    public Response passwordLost(@FormParam("username") String username) {
        authenticate();

        // Validate input data
        ValidationUtil.validateStringNotBlank("username", username);

        // Prepare response
        Response response = Response.ok().entity(Json.createObjectBuilder()
                .add("status", "ok")
                .build()).build();

        // Check for user existence
        UserDao userDao = new UserDao();
        List<UserDto> userDtoList = userDao.findByCriteria(new UserCriteria().setUserName(username), null);
        if (userDtoList.isEmpty()) {
            return response;
        }
        UserDto user = userDtoList.get(0);

        // Create the password recovery key
        PasswordRecoveryDao passwordRecoveryDao = new PasswordRecoveryDao();
        PasswordRecovery passwordRecovery = new PasswordRecovery();
        passwordRecovery.setUsername(user.getUsername());
        passwordRecoveryDao.create(passwordRecovery);

        // Fire a password lost event
        PasswordLostEvent passwordLostEvent = new PasswordLostEvent();
        passwordLostEvent.setUser(user);
        passwordLostEvent.setPasswordRecovery(passwordRecovery);
        AppContext.getInstance().getMailEventBus().post(passwordLostEvent);

        // Always return OK
        return response;
    }

    /**
     * Reset the user's password.
     *
     * @api {post} /user/password_reset Reset the user's password
     * @apiName PostUserPasswordReset
     * @apiGroup User
     * @apiParam {String} key Password recovery key
     * @apiParam {String} password New password
     * @apiSuccess {String} status Status OK
     * @apiError (client) KeyNotFound Password recovery key not found
     * @apiError (client) ValidationError Validation error
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param passwordResetKey Password reset key
     * @param password New password
     * @return Response
     */
    @POST
    @Path("password_reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response passwordReset(
            @FormParam("key") String passwordResetKey,
            @FormParam("password") String password) {
        authenticate();

        // Validate input data
        ValidationUtil.validateRequired("key", passwordResetKey);
        password = ValidationUtil.validateLength(password, "password", 8, 50, true);

        // Load the password recovery key
        PasswordRecoveryDao passwordRecoveryDao = new PasswordRecoveryDao();
        PasswordRecovery passwordRecovery = passwordRecoveryDao.getActiveById(passwordResetKey);
        if (passwordRecovery == null) {
            throw new ClientException("KeyNotFound", "Password recovery key not found");
        }

        UserDao userDao = new UserDao();
        User user = userDao.getActiveByUsername(passwordRecovery.getUsername());

        // Change the password
        user.setPassword(password);
        user = userDao.updatePassword(user, principal.getId());

        // Deletes password recovery requests
        passwordRecoveryDao.deleteActiveByLogin(user.getUsername());

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
                if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())
                        && !Strings.isNullOrEmpty(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
