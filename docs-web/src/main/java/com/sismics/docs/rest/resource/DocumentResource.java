package com.sismics.docs.rest.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.ShareDao;
import com.sismics.docs.core.dao.jpa.TagDao;
import com.sismics.docs.core.dao.jpa.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
import com.sismics.docs.core.dao.jpa.dto.TagDto;
import com.sismics.docs.core.event.DocumentCreatedAsyncEvent;
import com.sismics.docs.core.event.DocumentDeletedAsyncEvent;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.Share;
import com.sismics.docs.core.model.jpa.Tag;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
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
            @PathParam("id") String id,
            @QueryParam("share") String shareId) throws JSONException {
        authenticate();
        
        DocumentDao documentDao = new DocumentDao();
        ShareDao shareDao = new ShareDao();
        Document documentDb;
        try {
            documentDb = documentDao.getDocument(id);
            
            // Check document visibility
            if (!shareDao.checkVisibility(documentDb, principal.getId(), shareId)) {
                throw new ForbiddenClientException();
            }
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", id));
        }

        JSONObject document = new JSONObject();
        document.put("id", documentDb.getId());
        document.put("title", documentDb.getTitle());
        document.put("description", documentDb.getDescription());
        document.put("create_date", documentDb.getCreateDate().getTime());
        document.put("language", documentDb.getLanguage());
        
        // Add tags
        TagDao tagDao = new TagDao();
        List<TagDto> tagDtoList = tagDao.getByDocumentId(id);
        List<JSONObject> tags = new ArrayList<>();
        for (TagDto tagDto : tagDtoList) {
            JSONObject tag = new JSONObject();
            tag.put("id", tagDto.getId());
            tag.put("name", tagDto.getName());
            tag.put("color", tagDto.getColor());
            tags.add(tag);
        }
        document.put("tags", tags);
        
        // Add shares
        List<Share> shareDbList = shareDao.getByDocumentId(id);
        List<JSONObject> shareList = new ArrayList<>();
        for (Share shareDb : shareDbList) {
            JSONObject share = new JSONObject();
            share.put("id", shareDb.getId());
            share.put("name", shareDb.getName());
            shareList.add(share);
        }
        document.put("shares", shareList);
        
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
            @QueryParam("search") String search) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        JSONObject response = new JSONObject();
        List<JSONObject> documents = new ArrayList<>();
        
        DocumentDao documentDao = new DocumentDao();
        TagDao tagDao = new TagDao();
        PaginatedList<DocumentDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        DocumentCriteria documentCriteria = parseSearchQuery(search);
        documentCriteria.setUserId(principal.getId());
        try {
            documentDao.findByCriteria(paginatedList, documentCriteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in documents", e);
        }

        for (DocumentDto documentDto : paginatedList.getResultList()) {
            JSONObject document = new JSONObject();
            document.put("id", documentDto.getId());
            document.put("title", documentDto.getTitle());
            document.put("description", documentDto.getDescription());
            document.put("create_date", documentDto.getCreateTimestamp());
            document.put("shared", documentDto.getShared());
            document.put("language", documentDto.getLanguage());
            
            // Get tags
            List<TagDto> tagDtoList = tagDao.getByDocumentId(documentDto.getId());
            List<JSONObject> tags = new ArrayList<>();
            for (TagDto tagDto : tagDtoList) {
                JSONObject tag = new JSONObject();
                tag.put("id", tagDto.getId());
                tag.put("name", tagDto.getName());
                tag.put("color", tagDto.getColor());
                tags.add(tag);
            }
            document.put("tags", tags);
            
            documents.add(document);
        }
        response.put("total", paginatedList.getResultCount());
        response.put("documents", documents);
        
        return Response.ok().entity(response).build();
    }
    
    /**
     * Parse a query according to the specified syntax, eg.:
     * tag:assurance tag:other before:2012 after:2011-09 shared:yes lang:fra thing
     * 
     * @param search Search query
     * @return DocumentCriteria
     */
    private DocumentCriteria parseSearchQuery(String search) {
        DocumentCriteria documentCriteria = new DocumentCriteria();
        if (Strings.isNullOrEmpty(search)) {
            return documentCriteria;
        }
        
        TagDao tagDao = new TagDao();
        DateTimeParser[] parsers = { 
                DateTimeFormat.forPattern("yyyy").getParser(),
                DateTimeFormat.forPattern("yyyy-MM").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd").getParser() };
        DateTimeFormatter yearFormatter = new DateTimeFormatter(null, parsers[0]);
        DateTimeFormatter monthFormatter = new DateTimeFormatter(null, parsers[1]);
        DateTimeFormatter dayFormatter = new DateTimeFormatter(null, parsers[2]);
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().append( null, parsers ).toFormatter();
        
        String[] criteriaList = search.split("  *");
        List<String> query = new ArrayList<>();
        List<String> fullQuery = new ArrayList<>();
        for (String criteria : criteriaList) {
            String[] params = criteria.split(":");
            if (params.length != 2 || Strings.isNullOrEmpty(params[0]) || Strings.isNullOrEmpty(params[1])) {
                // This is not a special criteria
                query.add(criteria);
                continue;
            }
            
            if (params[0].equals("tag")) {
                // New tag criteria
                List<Tag> tagList = tagDao.findByName(principal.getId(), params[1]);
                if (documentCriteria.getTagIdList() == null) {
                    documentCriteria.setTagIdList(new ArrayList<String>());
                }
                if (tagList.size() == 0) {
                    // No tag found, the request must returns nothing
                    documentCriteria.getTagIdList().add(UUID.randomUUID().toString());
                }
                for (Tag tag : tagList) {
                    documentCriteria.getTagIdList().add(tag.getId());
                }
            } else if (params[0].equals("after") || params[0].equals("before")) {
                // New date span criteria
                try {
                    DateTime date = formatter.parseDateTime(params[1]);
                    if (params[0].equals("before")) documentCriteria.setCreateDateMax(date.toDate());
                    else documentCriteria.setCreateDateMin(date.toDate());
                } catch (IllegalArgumentException e) {
                    // NOP
                }
            } else if (params[0].equals("at")) {
                // New specific date criteria
                try {
                    if (params[1].length() == 10) {
                        DateTime date = dayFormatter.parseDateTime(params[1]);
                        documentCriteria.setCreateDateMin(date.toDate());
                        documentCriteria.setCreateDateMax(date.plusDays(1).toDate());
                    } else if (params[1].length() == 7) {
                        DateTime date = monthFormatter.parseDateTime(params[1]);
                        documentCriteria.setCreateDateMin(date.toDate());
                        documentCriteria.setCreateDateMax(date.plusMonths(1).toDate());
                    } else if (params[1].length() == 4) {
                        DateTime date = yearFormatter.parseDateTime(params[1]);
                        documentCriteria.setCreateDateMin(date.toDate());
                        documentCriteria.setCreateDateMax(date.plusYears(1).toDate());
                    }
                } catch (IllegalArgumentException e) {
                    // NOP
                }
            } else if (params[0].equals("shared")) {
                // New shared state criteria
                if (params[1].equals("yes")) {
                    documentCriteria.setShared(true);
                }
            } else if (params[0].equals("lang")) {
                // New language criteria
                if (Constants.SUPPORTED_LANGUAGES.contains(params[1])) {
                    documentCriteria.setLanguage(params[1]);
                }
            } else if (params[0].equals("full")) {
                // New full content search criteria
                fullQuery.add(params[1]);
            } else {
                query.add(criteria);
            }
        }
        
        documentCriteria.setSearch(Joiner.on(" ").join(query));
        documentCriteria.setFullSearch(Joiner.on(" ").join(fullQuery));
        return documentCriteria;
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
            @FormParam("tags") List<String> tagList,
            @FormParam("language") String language,
            @FormParam("create_date") String createDateStr) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        title = ValidationUtil.validateLength(title, "title", 1, 100, false);
        language = ValidationUtil.validateLength(language, "language", 3, 3, false);
        description = ValidationUtil.validateLength(description, "description", 0, 4000, true);
        Date createDate = ValidationUtil.validateDate(createDateStr, "create_date", true);
        if (!Constants.SUPPORTED_LANGUAGES.contains(language)) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} is not a supported language", language));
        }
        
        // Create the document
        DocumentDao documentDao = new DocumentDao();
        Document document = new Document();
        document.setUserId(principal.getId());
        document.setTitle(title);
        document.setDescription(description);
        document.setLanguage(language);
        if (createDate == null) {
            document.setCreateDate(new Date());
        } else {
            document.setCreateDate(createDate);
        }
        String documentId = documentDao.create(document);
        
        // Update tags
        updateTagList(documentId, tagList);
        
        // Raise a document created event
        DocumentCreatedAsyncEvent documentCreatedAsyncEvent = new DocumentCreatedAsyncEvent();
        documentCreatedAsyncEvent.setDocument(document);
        AppContext.getInstance().getAsyncEventBus().post(documentCreatedAsyncEvent);
        
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
            @FormParam("tags") List<String> tagList,
            @FormParam("language") String language,
            @FormParam("create_date") String createDateStr) throws JSONException {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        title = ValidationUtil.validateLength(title, "title", 1, 100, true);
        language = ValidationUtil.validateLength(language, "language", 3, 3, true);
        description = ValidationUtil.validateLength(description, "description", 0, 4000, true);
        Date createDate = ValidationUtil.validateDate(createDateStr, "create_date", true);
        if (language != null && !Constants.SUPPORTED_LANGUAGES.contains(language)) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} is not a supported language", language));
        }
        
        // Get the document
        DocumentDao documentDao = new DocumentDao();
        Document document;
        try {
            document = documentDao.getDocument(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", id));
        }
        
        // Update the document
        if (!StringUtils.isEmpty(title)) {
            document.setTitle(title);
        }
        if (!StringUtils.isEmpty(description)) {
            document.setDescription(description);
        }
        if (createDate != null) {
            document.setCreateDate(createDate);
        }
        if (language != null) {
            document.setLanguage(language);
        }
        
        // Update tags
        updateTagList(id, tagList);
        
        // Raise a document updated event
        DocumentUpdatedAsyncEvent documentUpdatedAsyncEvent = new DocumentUpdatedAsyncEvent();
        documentUpdatedAsyncEvent.setDocument(document);
        AppContext.getInstance().getAsyncEventBus().post(documentUpdatedAsyncEvent);
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("id", id);
        return Response.ok().entity(response).build();
    }

    /**
     * Update tags list on a document.
     * 
     * @param documentId Document ID
     * @param tagList Tag ID list
     * @throws JSONException
     */
    private void updateTagList(String documentId, List<String> tagList) throws JSONException {
        if (tagList != null) {
            TagDao tagDao = new TagDao();
            Set<String> tagSet = new HashSet<>();
            Set<String> tagIdSet = new HashSet<>();
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
        Document document;
        try {
            document = documentDao.getDocument(id, principal.getId());
        } catch (NoResultException e) {
            throw new ClientException("DocumentNotFound", MessageFormat.format("Document not found: {0}", id));
        }
        
        // Raise a document deleted event
        DocumentDeletedAsyncEvent documentDeletedAsyncEvent = new DocumentDeletedAsyncEvent();
        documentDeletedAsyncEvent.setDocument(document);
        AppContext.getInstance().getAsyncEventBus().post(documentDeletedAsyncEvent);
        
        // Delete the document
        documentDao.delete(document.getId());
        
        // Always return ok
        JSONObject response = new JSONObject();
        response.put("status", "ok");
        return Response.ok().entity(response).build();
    }
}
