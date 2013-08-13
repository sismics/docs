package com.sismics.docs.rest;

import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

/**
 * Test of the security layer.
 * 
 * @author jtremeaux
 */
public class TestSecurity extends BaseJerseyTest {
    /**
     * Test of the security layer.
     * 
     * @throws JSONException
     */
    @Test
    public void testSecurity() throws JSONException {
        // Create a user
        clientUtil.createUser("testsecurity");

        // Changes a user's email KO : the user is not connected
        WebResource userResource = resource().path("/user/update");
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("email", "testsecurity2@docs.com");
        ClientResponse response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ForbiddenError", json.getString("type"));
        Assert.assertEquals("You don't have access to this resource", json.getString("message"));

        // User testsecurity logs in
        String testSecurityAuthenticationToken = clientUtil.login("testsecurity");

        // User testsecurity changes his email OK
        userResource = resource().path("/user");
        userResource.addFilter(new CookieAuthenticationFilter(testSecurityAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        postParams.add("email", "testsecurity2@docs.com");
        postParams.add("locale", "en");
        response = userResource.post(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals("ok", json.getString("status"));

        // User testsecurity logs out
        userResource = resource().path("/user/logout");
        userResource.addFilter(new CookieAuthenticationFilter(testSecurityAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        response = userResource.post(ClientResponse.class, postParams);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        testSecurityAuthenticationToken = clientUtil.getAuthenticationCookie(response);
        Assert.assertTrue(StringUtils.isEmpty(testSecurityAuthenticationToken));

        // User testsecurity logs out KO : he is not connected anymore
        userResource = resource().path("/user/logout");
        userResource.addFilter(new CookieAuthenticationFilter(testSecurityAuthenticationToken));
        postParams = new MultivaluedMapImpl();
        response = userResource.post(ClientResponse.class, postParams);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // User testsecurity logs in with a long lived session
        testSecurityAuthenticationToken = clientUtil.login("testsecurity", "12345678", true);

        // User testsecurity logs out
        clientUtil.logout(testSecurityAuthenticationToken);
    }
}