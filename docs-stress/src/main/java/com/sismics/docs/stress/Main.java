package com.sismics.docs.stress;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.core.MediaType;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sismics.docs.rest.util.ClientUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Stress app for Sismics Docs.
 * 
 * @author bgamard
 */
public class Main {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    
    private static final String API_URL = "http://localhost:9999/docs-web/api/";
    private static final int USER_COUNT = 50;
    private static final int DOCUMENT_PER_USER_COUNT = 2000;
    private static final int TAG_PER_USER_COUNT = 20;
    private static final int FILE_PER_DOCUMENT_COUNT = 0;
    
    private static Client client = Client.create();
    private static ClientUtil clientUtil;
    
    private static Set<User> userSet = Sets.newHashSet();
    
    /**
     * Entry point.
     * 
     * @param args Args
     */
    public static void main(String[] args) {
        log.info("Starting stress test...");
        
        WebResource resource = client.resource(API_URL);
        clientUtil = new ClientUtil(resource);
        
        // Create users
        for (int i = 0; i < USER_COUNT; i++) {
            String username = generateString();
            clientUtil.createUser(username);
            userSet.add(new User(username, (clientUtil.login(username))));
            log.info("Created user " + (i + 1) + "/" + USER_COUNT);
        }
        
        // Create tags for each user
        int tagCreatedCount = 1;
        for (User user : userSet) {
            WebResource tagResource = resource.path("/tag");
            tagResource.addFilter(new CookieAuthenticationFilter(user.authToken));
            
            for (int j = 0; j < TAG_PER_USER_COUNT; j++) {
                MultivaluedMapImpl postParams = new MultivaluedMapImpl();
                String name = generateString();
                postParams.add("name", name);
                postParams.add("color", "#ff0000");
                ClientResponse response = tagResource.put(ClientResponse.class, postParams);
                JSONObject json = response.getEntity(JSONObject.class);
                user.tagList.add(json.optString("id"));
                log.info("Created tag " + (tagCreatedCount++) + "/" + TAG_PER_USER_COUNT * USER_COUNT);
            }
        }
        
        // Create documents for each user
        int documentCreatedCount = 1;
        for (User user : userSet) {
            for (int i = 0; i < DOCUMENT_PER_USER_COUNT; i++) {
                WebResource documentResource = resource.path("/document");
                documentResource.addFilter(new CookieAuthenticationFilter(user.authToken));
                MultivaluedMapImpl postParams = new MultivaluedMapImpl();
                postParams.add("title", generateString());
                postParams.add("description", generateString());
                postParams.add("tags", user.tagList.get(ThreadLocalRandom.current().nextInt(user.tagList.size()))); // Random tag
                postParams.add("language", "eng");
                long createDate = new Date().getTime();
                postParams.add("create_date", createDate);
                ClientResponse response = documentResource.put(ClientResponse.class, postParams);
                JSONObject json = response.getEntity(JSONObject.class);
                String documentId = json.optString("id");
                log.info("Created document " + (documentCreatedCount++) + "/" + DOCUMENT_PER_USER_COUNT * USER_COUNT + " for user: " + user.username);
                
                // Add files for each document
                for (int j = 0; j < FILE_PER_DOCUMENT_COUNT; j++) {
                    WebResource fileResource = resource.path("/file");
                    fileResource.addFilter(new CookieAuthenticationFilter(user.authToken));
                    FormDataMultiPart form = new FormDataMultiPart();
                    InputStream file = Main.class.getResourceAsStream("/empty.png");
                    FormDataBodyPart fdp = new FormDataBodyPart("file",
                            new BufferedInputStream(file),
                            MediaType.APPLICATION_OCTET_STREAM_TYPE);
                    form.bodyPart(fdp);
                    form.field("id", documentId);
                    response = fileResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);
                    Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
                }
            }
        }
    }
    
    private static String generateString() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    private static class User {
        public String username;
        public List<String> tagList = Lists.newArrayList();
        public String authToken;
        
        public User(String username, String authToken) {
            this.username = username;
            this.authToken = authToken;
        }
    }
}
