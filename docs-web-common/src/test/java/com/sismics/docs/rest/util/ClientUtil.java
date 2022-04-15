package com.sismics.docs.rest.util;

import com.google.common.io.Resources;
import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.Assert;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

/**
 * REST client utilities.
 *
 * @author jtremeaux 
 */
public class ClientUtil {
    private WebTarget resource;
    
    /**
     * Constructor of ClientUtil.
     * 
     * @param resource Resource corresponding to the base URI of REST resources.
     */
    public ClientUtil(WebTarget resource) {
        this.resource = resource;
    }

    public void createUser(String username, String... groupNameList) {
        createUser(username, 1000000, groupNameList); // 1MB quota by default
    }

    /**
     * Creates a user.
     * 
     * @param username Username
     * @param quota Quota
     * @param groupNameList Group names
     */
    public void createUser(String username, int quota, String... groupNameList) {
        // Login admin to create the user
        String adminToken = login("admin", "admin", false);
        
        // Create the user
        resource.path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("username", username)
                        .param("email", username + "@docs.com")
                        .param("password", "12345678")
                        .param("storage_quota", String.valueOf(quota))), JsonObject.class);
        
        // Add to groups
        for (String groupName : groupNameList) {
            resource.path("/group/" + groupName).request()
                    .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                    .put(Entity.form(new Form()
                            .param("username", username)), JsonObject.class);
        }
        
        // Logout admin
        logout(adminToken);
    }
    
    /**
     * Creates a group.
     * 
     * @param name Name
     */
    public void createGroup(String name) {
        createGroup(name, null);
    }
    
    /**
     * Creates a group.
     * 
     * @param name Name
     * @param parentId Parent ID
     */
    public void createGroup(String name, String parentId) {
        // Login admin to create the group
        String adminToken = login("admin", "admin", false);
        
        // Create the group
        resource.path("/group").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("name", name)
                        .param("parent", parentId)), JsonObject.class);
        
        // Logout admin
        logout(adminToken);
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
        Response response = resource.path("/user/login").request()
                .post(Entity.form(new Form()
                        .param("username", username)
                        .param("password", password)
                        .param("remember", remember.toString())));
        Assert.assertEquals(200, response.getStatus());

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
        resource.path("/user/logout").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, authenticationToken)
                .post(null);
    }

    /**
     * Extracts the authentication token of the response.
     * 
     * @param response Response
     * @return Authentication token
     */
    public String getAuthenticationCookie(Response response) {
        String authToken = null;
        for (NewCookie cookie : response.getCookies().values()) {
            if (TokenBasedSecurityFilter.COOKIE_NAME.equals(cookie.getName())) {
                authToken = cookie.getValue();
            }
        }
        return authToken;
    }

    /**
     * Create a document
     *
     * @param token Authentication token
     * @return Document ID
     */
    public String createDocument(String token) {
        JsonObject json = this.resource.path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, token)
                .put(Entity.form(new Form()
                        .param("title", "Document Title")
                        .param("description", "Document description")
                        .param("language", "eng")
                        .param("create_date", Long.toString(new Date().getTime()))), JsonObject.class);
        String documentId = json.getString("id");
        Assert.assertNotNull(documentId);
        return documentId;
    }

    /**
     * Add a file to a document.
     *
     * @param file File path
     * @param token Authentication token
     * @param documentId Document ID
     * @return File ID
     * @throws IOException e
     * @throws URISyntaxException e
     */
    public String addFileToDocument(String file, String token, String documentId) throws IOException, URISyntaxException {
        URL fileResource = Resources.getResource(file);
        Path filePath = Paths.get(fileResource.toURI());
        String filename = filePath.getFileName().toString();
        try (InputStream is = fileResource.openStream()) {
            StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file", is, filename);
            try (FormDataMultiPart multiPart = new FormDataMultiPart()) {
                MultiPart formContent;
                if (documentId != null) {
                    formContent = multiPart.field("id", documentId).bodyPart(streamDataBodyPart);
                } else {
                    formContent = multiPart.bodyPart(streamDataBodyPart);
                }
                JsonObject json = this.resource
                        .register(MultiPartFeature.class)
                        .path("/file").request()
                        .cookie(TokenBasedSecurityFilter.COOKIE_NAME, token)
                        .put(Entity.entity(formContent,
                                MediaType.MULTIPART_FORM_DATA_TYPE), JsonObject.class);
                String fileId = json.getString("id");
                Assert.assertNotNull(fileId);
                Assert.assertEquals(Files.size(filePath), json.getJsonNumber("size").longValue());
                return fileId;
            }
        }
    }
}
