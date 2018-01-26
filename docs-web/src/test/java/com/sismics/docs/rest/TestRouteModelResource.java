package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;


/**
 * Test the route model resource.
 * 
 * @author bgamard
 */
public class TestRouteModelResource extends BaseJerseyTest {
    /**
     * Test the route model resource.
     */
    @Test
    public void testRouteModelResource() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);
        
        // Get all route models
        JsonObject json = target().path("/routemodel")
                .queryParam("sort_column", "1")
                .queryParam("asc", "true")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        JsonArray groups = json.getJsonArray("routemodels");
        Assert.assertEquals(0, groups.size());
    }
}