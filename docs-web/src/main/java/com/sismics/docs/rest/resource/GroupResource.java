package com.sismics.docs.rest.resource;

import java.text.MessageFormat;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import com.sismics.docs.core.dao.jpa.GroupDao;
import com.sismics.docs.core.model.jpa.Group;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Group REST resources.
 * 
 * @author bgamard
 */
@Path("/group")
public class GroupResource extends BaseResource {
    /**
     * Add a group.
     * 
     * @return Response
     */
    @PUT
    public Response add(@FormParam("parent") String parentName,
            @FormParam("name") String name) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);
        
        // Validate input
        name = ValidationUtil.validateLength(name, "name", 1, 50, false);
        
        // Avoid duplicates
        GroupDao groupDao = new GroupDao();
        Group existingGroup = groupDao.getByName(name);
        if (existingGroup != null) {
            throw new ClientException("GroupAlreadyExists", MessageFormat.format("This group already exists: {0}", name));
        }
        
        // Validate parent
        String parentId = null;
        if (!Strings.isNullOrEmpty(parentName)) {
            Group parentGroup = groupDao.getByName(parentName);
            if (parentGroup == null) {
                throw new ClientException("ParentGroupNotFound", MessageFormat.format("This group doest not exists: {0}", parentName));
            }
            parentId = parentGroup.getId();
        }
        
        // Create the group
        groupDao.create(new Group()
                .setName(name)
                .setParentId(parentId), principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
