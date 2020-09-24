package com.sismics.docs.rest;

import com.sismics.util.filter.TokenBasedSecurityFilter;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Date;


/**
 * Test the ACL resource.
 *
 * @author bgamard
 */
public class TestAclResource extends BaseJerseyTest {
    /**
     * Test the ACL resource.
     */
    @Test
    public void testAclResource() {
        // Create aclGroup2
        clientUtil.createGroup("aclGroup2");

        // Login acl1
        clientUtil.createUser("acl1");
        String acl1Token = clientUtil.login("acl1");

        // Login acl2
        clientUtil.createUser("acl2", "aclGroup2");
        String acl2Token = clientUtil.login("acl2");

        // Create a document with acl1
        JsonObject json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("title", "My super title document 1")
                        .param("language", "eng")
                        .param("create_date", Long.toString(new Date().getTime()))), JsonObject.class);
        String document1Id = json.getString("id");

        // Get the document as acl1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        JsonArray acls = json.getJsonArray("acls");
        Assert.assertEquals(2, acls.size());

        // Get the document as acl2
        Response response = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));

        // List all documents with acl2
        json = target().path("/document/list")
                .queryParam("sort_column", 3)
                .queryParam("asc", true)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        JsonArray documents = json.getJsonArray("documents");
        Assert.assertEquals(0, documents.size());

        // Add an ACL READ for acl2 with acl1
        json = target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "READ")
                        .param("target", "acl2")
                        .param("type", "USER")), JsonObject.class);
        String acl2Id = json.getString("id");

        // Add an ACL WRITE for acl2 with acl1
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "WRITE")
                        .param("target", "acl2")
                        .param("type", "USER")), JsonObject.class);

        // List all documents with acl2
        json = target().path("/document/list")
                .queryParam("sort_column", 3)
                .queryParam("asc", true)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());

        // Add an ACL WRITE for acl2 with acl1 (again)
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "WRITE")
                        .param("target", "acl2")
                        .param("type", "USER")), JsonObject.class);

        // Add an ACL READ for aclGroup2 with acl1
        json = target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "READ")
                        .param("target", "aclGroup2")
                        .param("type", "GROUP")), JsonObject.class);
        String aclGroup2Id = json.getString("id");

        // Add an ACL WRITE for aclGroup2 with acl1
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .put(Entity.form(new Form()
                        .param("source", document1Id)
                        .param("perm", "WRITE")
                        .param("target", "aclGroup2")
                        .param("type", "GROUP")), JsonObject.class);

        // List all documents with acl2
        json = target().path("/document/list")
                .queryParam("sort_column", 3)
                .queryParam("asc", true)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());

        // Get the document as acl1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(6, acls.size());
        Assert.assertTrue(json.getBoolean("writable"));

        // Get the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(6, acls.size());
        Assert.assertTrue(json.getBoolean("writable"));

        // Update the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .post(Entity.form(new Form()
                        .param("title", "My new super document 1")
                        .param("language", "eng")), JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));

        // Get the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        JsonArray contributors = json.getJsonArray("contributors");
        Assert.assertEquals(2, contributors.size());

        // Delete the ACL WRITE for acl2 with acl2
        target().path("/acl/" + document1Id + "/WRITE/" + acl2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .delete(JsonObject.class);

        // Get the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(5, acls.size());
        Assert.assertTrue(json.getBoolean("writable")); // Writable by aclGroup2

        // Delete the ACL WRITE for aclGroup2 with acl2
        target().path("/acl/" + document1Id + "/WRITE/" + aclGroup2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .delete(JsonObject.class);

        // Get the document as acl2
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(4, acls.size());
        Assert.assertFalse(json.getBoolean("writable"));

        // Delete the ACL READ for acl2 with acl2 (not authorized)
        response = target().path("/acl/" + document1Id + "/READ/" + acl2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .delete();
        Assert.assertEquals(Status.FORBIDDEN, Status.fromStatusCode(response.getStatus()));

        // Delete the ACL READ for acl2 with acl1
        target().path("/acl/" + document1Id + "/READ/" + acl2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .delete(JsonObject.class);

        // Get the document as acl2 (visible by group)
        target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get(JsonObject.class);

        // Delete the ACL READ for aclGroup2 with acl1
        target().path("/acl/" + document1Id + "/READ/" + aclGroup2Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .delete(JsonObject.class);

        // Get the document as acl1
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        Assert.assertEquals(document1Id, json.getString("id"));
        acls = json.getJsonArray("acls");
        Assert.assertEquals(2, acls.size());
        String acl1Id = acls.getJsonObject(0).getString("id");

        // Get the document as acl2
        response = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl2Token)
                .get();
        Assert.assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));

        // Delete the ACL READ for acl1 with acl1
        response = target().path("/acl/" + document1Id + "/READ/" + acl1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));

        // Delete the ACL WRITE for acl1 with acl1
        response = target().path("/acl/" + document1Id + "/WRITE/" + acl1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .delete();
        Assert.assertEquals(Status.BAD_REQUEST, Status.fromStatusCode(response.getStatus()));

        // Search target list (acl)
        json = target().path("/acl/target/search")
                .queryParam("search", "acl")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        JsonArray users = json.getJsonArray("users");
        Assert.assertTrue(users.size() > 0);
        JsonArray groups = json.getJsonArray("groups");
        Assert.assertTrue(groups.size() > 0);

        // Search target list (admin)
        json = target().path("/acl/target/search")
                .queryParam("search", "admin")
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acl1Token)
                .get(JsonObject.class);
        users = json.getJsonArray("users");
        Assert.assertEquals(0, users.size());
        groups = json.getJsonArray("groups");
        Assert.assertEquals(0, groups.size());
    }

    @Test
    public void testAclTags() {
        // Login acltag1
        clientUtil.createUser("acltag1");
        String acltag1Token = clientUtil.login("acltag1");

        // Login acltag2
        clientUtil.createUser("acltag2");
        String acltag2Token = clientUtil.login("acltag2");

        // Create tag1 with acltag1
        JsonObject json = target().path("/tag").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag1Token)
                .put(Entity.form(new Form()
                        .param("name", "AclTag1")
                        .param("color", "#ff0000")), JsonObject.class);
        String tag1Id = json.getString("id");
        Assert.assertNotNull(tag1Id);

        // Create document1 with acltag1 tagged with tag1
        json = target().path("/document").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag1Token)
                .put(Entity.form(new Form()
                        .param("title", "My super document 1")
                        .param("tags", tag1Id)
                        .param("language", "eng")), JsonObject.class);
        String document1Id = json.getString("id");

        // acltag2 cannot see document1
        Response response = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get();
        Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

        // acltag2 cannot see any tag
        json = target().path("/tag/list").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get(JsonObject.class);
        JsonArray tags = json.getJsonArray("tags");
        Assert.assertEquals(0, tags.size());

        // acltag2 cannot see tag1
        response = target().path("/tag/" + tag1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get();
        Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

        // acltag2 cannot see any document
        json = target().path("/document/list")
                .queryParam("sort_column", 3)
                .queryParam("asc", true)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get(JsonObject.class);
        JsonArray documents = json.getJsonArray("documents");
        Assert.assertEquals(0, documents.size());

        // acltag2 cannot edit tag1
        response = target().path("/tag/" + tag1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .post(Entity.form(new Form()
                        .param("name", "AclTag1")
                        .param("color", "#ff0000")));
        Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

        // acltag2 cannot edit document1
        response = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .post(Entity.form(new Form()
                        .param("title", "My super document 1")
                        .param("tags", tag1Id)
                        .param("language", "eng")));
        Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());

        // Add an ACL READ for acltag2 with acltag1 on tag1
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag1Token)
                .put(Entity.form(new Form()
                        .param("source", tag1Id)
                        .param("perm", "READ")
                        .param("target", "acltag2")
                        .param("type", "USER")), JsonObject.class);

        // acltag2 can see tag1
        json = target().path("/tag/" + tag1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get(JsonObject.class);
        Assert.assertFalse(json.getBoolean("writable"));
        Assert.assertEquals(3, json.getJsonArray("acls").size());

        // acltag2 still cannot edit tag1
        response = target().path("/tag/" + tag1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .post(Entity.form(new Form()
                        .param("name", "AclTag1")
                        .param("color", "#ff0000")));
        Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

        // acltag2 still cannot edit document1
        response = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .post(Entity.form(new Form()
                        .param("title", "My super document 1")
                        .param("tags", tag1Id)
                        .param("language", "eng")));
        Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());

        // acltag2 can see document1 with tag1 (non-writable)
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertEquals(1, tags.size());
        Assert.assertFalse(json.getBoolean("writable"));
        Assert.assertEquals(tag1Id, tags.getJsonObject(0).getString("id"));
        JsonArray inheritedAcls = json.getJsonArray("inherited_acls");
        Assert.assertEquals(3, inheritedAcls.size());
        Assert.assertEquals("AclTag1", inheritedAcls.getJsonObject(0).getString("source_name"));
        Assert.assertEquals(tag1Id, inheritedAcls.getJsonObject(0).getString("source_id"));

        // acltag2 can see tag1
        json = target().path("/tag/list").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertEquals(1, tags.size());
        Assert.assertEquals(tag1Id, tags.getJsonObject(0).getString("id"));

        // acltag2 can see exactly one document
        json = target().path("/document/list")
                .queryParam("sort_column", 3)
                .queryParam("asc", true)
                .request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get(JsonObject.class);
        documents = json.getJsonArray("documents");
        Assert.assertEquals(1, documents.size());

        // Add an ACL WRITE for acltag2 with acltag1 on tag1
        target().path("/acl").request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag1Token)
                .put(Entity.form(new Form()
                        .param("source", tag1Id)
                        .param("perm", "WRITE")
                        .param("target", "acltag2")
                        .param("type", "USER")), JsonObject.class);

        // acltag2 can see document1 with tag1 (writable)
        json = target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get(JsonObject.class);
        tags = json.getJsonArray("tags");
        Assert.assertEquals(1, tags.size());
        Assert.assertTrue(json.getBoolean("writable"));
        Assert.assertEquals(tag1Id, tags.getJsonObject(0).getString("id"));
        inheritedAcls = json.getJsonArray("inherited_acls");
        Assert.assertEquals(4, inheritedAcls.size());
        Assert.assertEquals("AclTag1", inheritedAcls.getJsonObject(0).getString("source_name"));
        Assert.assertEquals(tag1Id, inheritedAcls.getJsonObject(0).getString("source_id"));

        // acltag2 can see and edit tag1
        json = target().path("/tag/" + tag1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .get(JsonObject.class);
        Assert.assertTrue(json.getBoolean("writable"));
        Assert.assertEquals(4, json.getJsonArray("acls").size());

        // acltag2 can edit tag1
        target().path("/tag/" + tag1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .post(Entity.form(new Form()
                        .param("name", "AclTag1")
                        .param("color", "#ff0000")), JsonObject.class);

        // acltag2 can edit document1
        target().path("/document/" + document1Id).request()
                .cookie(TokenBasedSecurityFilter.COOKIE_NAME, acltag2Token)
                .post(Entity.form(new Form()
                        .param("title", "My super document 1")
                        .param("tags", tag1Id)
                        .param("language", "eng")), JsonObject.class);
    }
}