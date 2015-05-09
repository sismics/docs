package com.sismics.docs.rest;

import java.util.Date;

import junit.framework.Assert;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sismics.docs.rest.filter.CookieAuthenticationFilter;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Test the ACL resource.
 * 
 * @author bgamard
 */
public class TestAclResource extends BaseJerseyTest {
    /**
     * Test the ACL resource.
     * 
     * @throws JSONException
     */
    @Test
    public void testAclResource() throws JSONException {
        // Login acl1
        clientUtil.createUser("acl1");
        String acl1Token = clientUtil.login("acl1");
        
        // Login acl2
        clientUtil.createUser("acl2");
        String acl2Token = clientUtil.login("acl2");
        
        // Create a document
        WebResource documentResource = resource().path("/document");
        documentResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        MultivaluedMapImpl postParams = new MultivaluedMapImpl();
        postParams.add("title", "My super title document 1");
        postParams.add("language", "eng");
        postParams.add("create_date", new Date().getTime());
        ClientResponse response = documentResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONObject json = response.getEntity(JSONObject.class);
        String document1Id = json.optString("id");
        
        // Get the document as acl1
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(document1Id, json.getString("id"));
        JSONArray acls = json.getJSONArray("acls");
        Assert.assertEquals(2, acls.length());
        
        // Get the document as acl2
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(acl2Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
        
        // Add an ACL READ for acl2 with acl1
        WebResource aclResource = resource().path("/acl");
        aclResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("source", document1Id);
        postParams.add("perm", "READ");
        postParams.add("username", "acl2");
        response = aclResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        json = response.getEntity(JSONObject.class);
        String acl2Id = json.getString("id");
        
        // Add an ACL WRITE for acl2 with acl1
        aclResource = resource().path("/acl");
        aclResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("source", document1Id);
        postParams.add("perm", "WRITE");
        postParams.add("username", "acl2");
        response = aclResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Add an ACL WRITE for acl2 with acl1 (again)
        aclResource = resource().path("/acl");
        aclResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        postParams = new MultivaluedMapImpl();
        postParams.add("source", document1Id);
        postParams.add("perm", "WRITE");
        postParams.add("username", "acl2");
        response = aclResource.put(ClientResponse.class, postParams);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Get the document as acl1
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJSONArray("acls");
        Assert.assertEquals(4, acls.length());
        
        // Get the document as acl2
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(acl2Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJSONArray("acls");
        Assert.assertEquals(4, acls.length());
        
        // Delete the ACL WRITE for acl2 with acl2
        aclResource = resource().path("/acl/" + document1Id + "/WRITE/" + acl2Id);
        aclResource.addFilter(new CookieAuthenticationFilter(acl2Token));
        response = aclResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Delete the ACL READ for acl2 with acl2
        aclResource = resource().path("/acl/" + document1Id + "/READ/" + acl2Id);
        aclResource.addFilter(new CookieAuthenticationFilter(acl2Token));
        response = aclResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
        
        // Delete the ACL READ for acl2 with acl1
        aclResource = resource().path("/acl/" + document1Id + "/READ/" + acl2Id);
        aclResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        response = aclResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        
        // Get the document as acl1
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJSONArray("acls");
        Assert.assertEquals(2, acls.length());
        String acl1Id = acls.getJSONObject(0).getString("id");
        
        // Get the document as acl2
        documentResource = resource().path("/document/" + document1Id);
        documentResource.addFilter(new CookieAuthenticationFilter(acl2Token));
        response = documentResource.get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));
        
        // Delete the ACL READ for acl1 with acl1
        aclResource = resource().path("/acl/" + document1Id + "/READ/" + acl1Id);
        aclResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        response = aclResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        
        // Delete the ACL WRITE for acl1 with acl1
        aclResource = resource().path("/acl/" + document1Id + "/WRITE/" + acl1Id);
        aclResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        response = aclResource.delete(ClientResponse.class);
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));
        
        // Search target list
        aclResource = resource().path("/acl/target/search");
        aclResource.addFilter(new CookieAuthenticationFilter(acl1Token));
        response = aclResource.queryParam("search", "acl").get(ClientResponse.class);
        json = response.getEntity(JSONObject.class);
        Assert.assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        JSONArray users = json.getJSONArray("users");
        Assert.assertEquals(2, users.length());
    }
}