package com.sismics.docs.rest;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;

import org.junit.Assert;
import org.junit.Test;

import com.sismics.util.filter.TokenBasedSecurityFilter;

/**
 * Exhaustive test of the vocabulary resource.
 * 
 * @author bgamard
 */
public class TestVocabularyResource extends BaseJerseyTest {
    /**
     * Test the vocabulary resource.
     * 
     * @throws Exception 
     */
    @Test
    public void testVocabularyResource() throws Exception {
        // Login vocabulary1
        clientUtil.createUser("vocabulary1");
        String vocabulary1Token = clientUtil.login("vocabulary1");
        
        // Create a vocabulary entry with vocabulary1
        JsonObject json = target().path("/vocabulary").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, vocabulary1Token)
                .put(Entity.form(new Form()
                        .param("name", "TEST_VOC_1")
                        .param("value", "First value")
                        .param("order", "0")), JsonObject.class);
        String vocabulary1Id = json.getString("id");
        Assert.assertNotNull(vocabulary1Id);
    }
}