package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


/**
 * Test the group resource.
 * 
 * @author bgamard
 */
public class TestGroupResource extends BaseJerseyTest {
    /**
     * Test the group resource.
     */
    @Test
    public void testGroupResource() {
        // Login admin
        String adminToken = clientUtil.login("admin", "admin", false);
        
        // Create group hierarchy
        clientUtil.createGroup("g1");
        clientUtil.createGroup("g11", "g1");
        clientUtil.createGroup("g12", "g1");
        clientUtil.createGroup("g111", "g11");
        clientUtil.createGroup("g112", "g11");
        
        // Login group1
        clientUtil.createUser("group1", "g112", "g12");
        String group1Token = clientUtil.login("group1");
        
        // Login admin2
        clientUtil.createUser("admin2", "administrators");
        String admin2Token = clientUtil.login("admin2");
        
        // Create trashme
        clientUtil.createUser("trashme");
        
        // Delete trashme with admin2
        target().path("/user/trashme").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, admin2Token)
                .delete(JsonObject.class);
        
        // Get all groups
        JsonObject json = target().path("/group")
                .queryParam("sort_column", "1")
                .queryParam("asc", "true")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        JsonArray groups = json.getJsonArray("groups");
        Assert.assertEquals(6, groups.size());
        JsonObject groupG11 = groups.getJsonObject(2);
        Assert.assertEquals("g11", groupG11.getString("name"));
        Assert.assertEquals("g1", groupG11.getString("parent"));
        
        // Check admin groups (all computed groups)
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        groups = json.getJsonArray("groups");
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals("administrators", groups.getString(0));
        
        // Check group1 groups (all computed groups)
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, group1Token)
                .get(JsonObject.class);
        groups = json.getJsonArray("groups");
        List<String> groupList = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            groupList.add(groups.getString(i));
        }
        Assert.assertEquals(4, groups.size());
        Assert.assertTrue(groupList.contains("g1"));
        Assert.assertTrue(groupList.contains("g12"));
        Assert.assertTrue(groupList.contains("g11"));
        Assert.assertTrue(groupList.contains("g112"));
        
        // Check group1 groups with admin (only direct groups)
        json = target().path("/user/group1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        groups = json.getJsonArray("groups");
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals("g112", groups.getString(0));
        Assert.assertEquals("g12", groups.getString(1));
        
        // List all users in group1
        json = target().path("/user/list")
                .queryParam("group", "g112")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        JsonArray users = json.getJsonArray("users");
        Assert.assertEquals(1, users.size());
        
        // Add group1 to g112 (again)
        target().path("/group/g112").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .put(Entity.form(new Form()
                        .param("username", "group1")), JsonObject.class);
        
        // Check group1 groups (all computed groups)
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, group1Token)
                .get(JsonObject.class);
        groups = json.getJsonArray("groups");
        Assert.assertEquals(4, groups.size());
        
        // Update group g12
        target().path("/group/g12").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .post(Entity.form(new Form()
                        .param("name", "g12new")
                        .param("parent", "g11")), JsonObject.class);
        
        // Check group1 groups with admin (only direct groups)
        json = target().path("/user/group1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        groups = json.getJsonArray("groups");
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals("g112", groups.getString(0));
        Assert.assertEquals("g12new", groups.getString(1));
        
        // Get group g12new
        json = target().path("/group/g12new").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .get(JsonObject.class);
        Assert.assertEquals("g12new", json.getString("name"));
        Assert.assertEquals("g11", json.getString("parent"));
        JsonArray members = json.getJsonArray("members");
        Assert.assertEquals(1, members.size());
        Assert.assertEquals("group1", members.getString(0));
        
        // Remove group1 from g12new
        target().path("/group/g12new/group1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete(JsonObject.class);
        
        // Check group1 groups (all computed groups)
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, group1Token)
                .get(JsonObject.class);
        groups = json.getJsonArray("groups");
        groupList = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            groupList.add(groups.getString(i));
        }
        Assert.assertEquals(3, groups.size());
        Assert.assertTrue(groupList.contains("g1"));
        Assert.assertTrue(groupList.contains("g11"));
        Assert.assertTrue(groupList.contains("g112"));
        
        // Delete group g1
        target().path("/group/g1").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete(JsonObject.class);

        // Delete group administrators
        Response response = target().path("/group/administrators").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, adminToken)
                .delete();
        Assert.assertEquals(Response.Status.BAD_REQUEST, Response.Status.fromStatusCode(response.getStatus()));
        json = response.readEntity(JsonObject.class);
        Assert.assertEquals("ForbiddenError", json.getString("type"));
        Assert.assertEquals("The administrators group cannot be deleted", json.getString("message"));
        
        // Check group1 groups (all computed groups)
        json = target().path("/user").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, group1Token)
                .get(JsonObject.class);
        groups = json.getJsonArray("groups");
        groupList = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            groupList.add(groups.getString(i));
        }
        Assert.assertEquals(2, groups.size());
        Assert.assertTrue(groupList.contains("g11"));
        Assert.assertTrue(groupList.contains("g112"));
    }
}