package com.sismics.docs.stress;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;

import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.sismics.docs.rest.util.ClientUtil;
import com.sismics.util.filter.TokenBasedSecurityFilter;

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
    
    private static Client client = ClientBuilder.newClient();
    private static ClientUtil clientUtil;
    
    private static Set<User> userSet = Sets.newHashSet();
    
    /**
     * Entry point.
     * 
     * @param args Args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        log.info("Starting stress test...");
        
        WebTarget resource = client.target(API_URL);
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
            Invocation.Builder tagResource = resource.path("/tag").request()
                    .cookie(TokenBasedSecurityFilter.COOKIE_NAME, user.authToken);
            
            for (int j = 0; j < TAG_PER_USER_COUNT; j++) {
                Form form = new Form();
                String name = generateString();
                form.param("name", name);
                form.param("color", "#ff0000");
                JsonObject json = tagResource.put(Entity.form(form), JsonObject.class);
                user.tagList.add(json.getString("id"));
                log.info("Created tag " + (tagCreatedCount++) + "/" + TAG_PER_USER_COUNT * USER_COUNT);
            }
        }
        
        // Create documents for each user
        int documentCreatedCount = 1;
        for (User user : userSet) {
            for (int i = 0; i < DOCUMENT_PER_USER_COUNT; i++) {
                long createDate = new Date().getTime();
                Form form = new Form()
                        .param("title", generateString())
                        .param("description", generateString())
                        .param("tags", user.tagList.get(ThreadLocalRandom.current().nextInt(user.tagList.size()))) // Random tag
                        .param("language", "eng")
                        .param("create_date", Long.toString(createDate));
                JsonObject json = resource.path("/document").request()
                        .cookie(TokenBasedSecurityFilter.COOKIE_NAME, user.authToken)
                        .put(Entity.form(form), JsonObject.class);
                String documentId = json.getString("id");
                log.info("Created document " + (documentCreatedCount++) + "/" + DOCUMENT_PER_USER_COUNT * USER_COUNT + " for user: " + user.username);
                
                // Add files for each document
                for (int j = 0; j < FILE_PER_DOCUMENT_COUNT; j++) {
                    try (InputStream is = Resources.getResource("empty.png").openStream()) {
                        StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart("file", is, "empty.png");
                        @SuppressWarnings("resource")
                        ClientResponse response = resource
                                .register(MultiPartFeature.class)
                                .path("/file").request()
                                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, user.authToken)
                                .put(Entity.entity(new FormDataMultiPart().field("id", documentId).bodyPart(streamDataBodyPart),
                                        MediaType.MULTIPART_FORM_DATA_TYPE), ClientResponse.class);
                        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
                    }
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
