package com.sismics.docs.rest.util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import junit.framework.Assert;

import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * REST client utilities.
 *
 * @author jtremeaux 
 */
public class ClientUtil {
    private WebResource resource;
    
    /**
     * Constructor of ClientUtil.
     * 
     * @param webResource Resource corresponding to the base URI of REST resources.
     */
    public ClientUtil(WebResource resource) {
        this.resource = resource;
    }
    
    /**
     * Creates a user.
     * 
     * @param username Username
     */
    public void createUser(String username) {
        // Login admin to create the user
        String adminAuthenticationToken = login("admin", "admin", false);
        
        // Create the user
        WebResource userResource = resource.path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(adminAuthenticationToken));
        MultivaluedMap<String, String> postParams = new MultivaluedMapImpl();
        postParams.putSingle("username", username);
        postParams.putSingle("email", username + "@docs.com");
        postParams.putSingle("password", "12345678");
        postParams.putSingle("time_zone", "Asia/Tokyo");
        ClientResponse response = userResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Logout admin
        logout(adminAuthenticationToken);
    }
    
    /**
     * Connects a user to the application.
     * 
     * @param username Username
     * @param password Password
     * @param remember Remember user
     * @return Authentication token
     */
    public String login(String username, String password, Boolean remember) {
        WebResource userResource = resource.path("/user/login");
        MultivaluedMap<String, String> postParams = new MultivaluedMapImpl();
        postParams.putSingle("username", username);
        postParams.putSingle("password", password);
        postParams.putSingle("remember", remember.toString());
        ClientResponse response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        return getAuthenticationCookie(response);
    }

    /**
     * Connects a user to the application.
     * 
     * @param username Username
     * @return Authentication token
     */
    public String login(String username) {
        return login(username, "12345678", false);
    }
    
    /**
     * Disconnects a user from the application.
     * 
     * @param authenticationToken Authentication token
     */
    public void logout(String authenticationToken) {
        WebResource userResource = resource.path("/user/logout");
        userResource.addFilter(new CookieAuthenticationFilter(authenticationToken));
        ClientResponse response = userResource.post(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
    }

    /**
     * Extracts the authentication token of the response.
     * 
     * @param response Response
     * @return Authentication token
     */
    public String getAuthenticationCookie(ClientResponse response) {
        String authToken = null;
        for (NewCookie cookie : response.getCookies()) {
            if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                authToken = cookie.getValue();
            }
        }
        return authToken;
    }
}
