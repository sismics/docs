package com.sismics.docs.rest.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.base.Strings;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.TagDao;
import com.sismics.docs.core.dao.jpa.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
import com.sismics.docs.core.dao.jpa.dto.TagDto;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.Tag;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;

/**
 * Document REST resources.
 * 
 * @author bgamard
 */
@Path("/document")
public class DocumentResource extends BaseResource {
    /**
     * Returns a document.
     * 
     * @param id Document ID
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        DocumentDao documentDao = new DocumentDao();
        Document documentDb = null;
        try {
            documentDb = documentDao.getDocument(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", id));
        }

        JSONObject document = new JSONObject();
        document.put("id", documentDb.getId());
        document.put("title", documentDb.getTitle());
        document.put("description", documentDb.getDescription());
        document.put("create_date", documentDb.getCreateDate().getTime());
        
        // Get tags
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.getByDocumentId(id);
        List<JSONObject> tags = new ArrayList<JSONObject>();
        for (TagDto tagDto : tagDtoList) {
            JSONObject tag = new JSONObject();
            tag.put("id", tagDto.getId());
            tag.put("name", tagDto.getName());
            tags.add(tag);
        }
        document.put("tags", tags);
        
        return Response.ok().entity(document).build();
    }
    
    /**
     * Returns all documents.
     * 
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     * @throws JSONException
     */
    @GET
    @Path("list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search,
            @QueryParam("create_date_min") String createDateMinStr,
            @QueryParam("create_date_max") String createDateMaxStr,
            @QueryParam("tags[]") List<String> tagIdList) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        Date createDateMin = ValidationUtil.validateDate(createDateMinStr, "create_date_min", true);
        Date createDateMax = ValidationUtil.validateDate(createDateMaxStr, "create_date_max", true);
        
        JSONObject response = new JSONObject();
        List<JSONObject> documents = new ArrayList<JSONObject>();
        
        DocumentDao documentDao = new DocumentDao();
        PaginatedList<DocumentDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        DocumentCriteria documentCriteria = new DocumentCriteria();
        documentCriteria.setUserId(principal.getId());
        documentCriteria.setCreateDateMin(createDateMin);
        documentCriteria.setCreateDateMax(createDateMax);
        documentCriteria.setTagIdList(tagIdList);
        if (!Strings.isNullOrEmpty(search)) {
            documentCriteria.setSearch(search);
        }
        documentDao.findByCriteria(paginatedList, documentCriteria, sortCriteria);

        for (DocumentDto documentDto : paginatedList.getResultList()) {
            JSONObject document = new JSONObject();
            document.put("id", documentDto.getId());
            document.put("title", documentDto.getTitle());
            document.put("description", documentDto.getDescription());
            document.put("create_date", documentDto.getCreateTimestamp());
            documents.add(document);
        }
        response.put("total", paginatedList.getResultCount());
        response.put("documents", documents);
        
        return Response.ok().entity(response).build();
    }
    
    /**
     * Creates a new document.
     * 
     * @param title Title
     * @param description Description
     * @return Response
     * @throws JSONException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(
            @FormParam("title") String title,
            @FormParam("description") String description,
            @FormParam("tags[]") List<String> tagList,
            @FormParam("create_date") String createDateStr) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        title = ValidationUtil.validateLength(title, "title", 1, 100, false);
        description = ValidationUtil.validateLength(description, "description", 0, 4000, true);
        Date createDate = ValidationUtil.validateDate(createDateStr, "create_date", true);
        
        // Create the document
        DocumentDao documentDao = new DocumentDao();
        Document document = new Document();
        document.setUserId(principal.getId());
        document.setTitle(title);
        document.setDescription(description);
        if (createDate == null) {
            document.setCreateDate(new Date());
        } else {
            document.setCreateDate(createDate);
        }
        String documentId = documentDao.create(document);
        
        // Update tags
        updateTagList(documentId, tagList);
        
        JSONObject response = new JSONObject();
        response.put("id", documentId);
        return Response.ok().entity(response).build();
    }
    
    /**
     * Updates the document.
     * 
     * @param title Title
     * @param description Description
     * @return Response
     * @throws JSONException
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") String id,
            @FormParam("title") String title,
            @FormParam("description") String description,
            @FormParam("tags[]") List<String> tagList,
            @FormParam("create_date") String createDateStr) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        title = ValidationUtil.validateLength(title, "title", 1, 100, false);
        description = ValidationUtil.validateLength(description, "description", 0, 4000, true);
        Date createDate = ValidationUtil.validateDate(createDateStr, "create_date", true);
        
        // Get the document
        DocumentDao documentDao = new DocumentDao();
        Document document = null;
        try {
            document = documentDao.getDocument(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", id));
        }
        
        // Update the document
        if (title != null) {
            document.setTitle(title);
        }
        if (description != null) {
            document.setDescription(description);
        }
        if (createDate != null) {
            document.setCreateDate(createDate);
        }
        
        // Update tags
        updateTagList(id, tagList);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("id", id);
        return Response.ok().entity(response).build();
    }

    /**
     * Update tags list on a document.
     * 
     * @param documentId
     * @param tagList
     * @throws JSONException
     */
    private void updateTagList(String documentId, List<String> tagList) throws JSONException {
        if (tagList != null) {
            TagDao tagDao = new TagDao();
            Set<String> tagSet = new HashSet<String>();
            Set<String> tagIdSet = new HashSet<String>();
            List<Tag> tagDbList = tagDao.getByUserId(principal.getId());
            for (Tag tagDb : tagDbList) {
                tagIdSet.add(tagDb.getId());
            }
            for (String tagId : tagList) {
                if (!tagIdSet.contains(tagId)) {
                    throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
                }
                tagSet.add(tagId);
            }
            tagDao.updateTagList(documentId, tagSet);
        }
    }
    
    /**
     * Deletes a document.
     * 
     * @param id Document ID
     * @return Response
     * @throws JSONException
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(
            @PathParam("id") String id) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the document
        DocumentDao documentDao = new DocumentDao();
        Document document = null;
        try {
            document = documentDao.getDocument(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", id));
        }
        
        // Delete the document
        documentDao.delete(document.getId());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
