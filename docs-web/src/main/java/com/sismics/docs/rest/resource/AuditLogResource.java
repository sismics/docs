package com.sismics.docs.rest.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.AuditLogDao;
import com.sismics.docs.core.dao.jpa.criteria.AuditLogCriteria;
import com.sismics.docs.core.dao.jpa.dto.AuditLogDto;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;

/**
 * Audit log REST resources.
 * 
 * @author bgamard
 */
@Path("/auditlog")
public class AuditLogResource extends BaseResource {
    /**
     * Returns the list of all logs for a document or user.
     * 
     * @return Response
     * @throws JSONException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@QueryParam("document") String documentId) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // On a document or a user?
        PaginatedList<AuditLogDto> paginatedList = PaginatedLists.create(20, 0);
        SortCriteria sortCriteria = new SortCriteria(1, false);
        AuditLogCriteria criteria = new AuditLogCriteria();
        if (documentId == null) {
            // Search logs for a user
            criteria.setUserId(principal.getId());
        } else {
            // Check ACL on the document
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(documentId, PermType.READ, principal.getId())) {
                return Response.status(Status.NOT_FOUND).build();
            }
            criteria.setDocumentId(documentId);
        }
        
        // Search the logs
        try {
            AuditLogDao auditLogDao = new AuditLogDao();
            auditLogDao.findByCriteria(paginatedList, criteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in logs", e);
        }
        
        // Assemble the results
        List<JSONObject> logs = new ArrayList<>();
        JSONObject response = new JSONObject();
        for (AuditLogDto auditLogDto : paginatedList.getResultList()) {
            JSONObject log = new JSONObject();
            log.put("id", auditLogDto.getId());
            log.put("target", auditLogDto.getEntityId());
            log.put("class", auditLogDto.getEntityClass());
            log.put("type", auditLogDto.getType().name());
            log.put("message", auditLogDto.getMessage());
            log.put("create_date", auditLogDto.getCreateTimestamp());
            logs.add(log);
        }

        // Send the response
        response.put("logs", logs);
        response.put("total", paginatedList.getResultCount());
        return Response.ok().entity(response).build();
    }
}
