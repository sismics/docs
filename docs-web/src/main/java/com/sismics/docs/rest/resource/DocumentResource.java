package com.sismics.docs.rest.resource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.AclDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.FileDao;
import com.sismics.docs.core.dao.jpa.TagDao;
import com.sismics.docs.core.dao.jpa.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.jpa.dto.AclDto;
import com.sismics.docs.core.dao.jpa.dto.DocumentDto;
import com.sismics.docs.core.dao.jpa.dto.TagDto;
import com.sismics.docs.core.event.DocumentCreatedAsyncEvent;
import com.sismics.docs.core.event.DocumentDeletedAsyncEvent;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Acl;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.Tag;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.JsonUtil;
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
     * @param documentId Document ID
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    public Response get(
            @PathParam("id") String documentId,
            @QueryParam("share") String shareId) {
        authenticate();
        
        DocumentDao documentDao = new DocumentDao();
        AclDao aclDao = new AclDao();
        DocumentDto documentDto = documentDao.getDocument(documentId);
        if (documentDto == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
            
        // Check document visibility
        if (!aclDao.checkPermission(documentId, PermType.READ, shareId == null ? principal.getId() : shareId)) {
            throw new ForbiddenClientException();
        }

        JsonObjectBuilder document = Json.createObjectBuilder()
                .add("id", documentDto.getId())
                .add("title", documentDto.getTitle())
                .add("description", JsonUtil.nullable(documentDto.getDescription()))
                .add("create_date", documentDto.getCreateTimestamp())
                .add("language", documentDto.getLanguage())
                .add("shared", documentDto.getShared())
                .add("file_count", documentDto.getFileCount());
        
        if (principal.isAnonymous()) {
            // No tags in anonymous mode (sharing)
            document.add("tags", Json.createArrayBuilder());
        } else {
            // Add tags added by the current user on this document
            TagDao tagDao = new TagDao();
            List<TagDto> tagDtoList = tagDao.getByDocumentId(documentId, principal.getId());
            JsonArrayBuilder tags = Json.createArrayBuilder();
            for (TagDto tagDto : tagDtoList) {
                tags.add(Json.createObjectBuilder()
                        .add("id", tagDto.getId())
                        .add("name", tagDto.getName())
                        .add("color", tagDto.getColor()));
            }
            document.add("tags", tags);
        }
        
        // Below is specific to GET /document/id
        
        document.add("creator", documentDto.getCreator());
        
        // Add ACL
        List<AclDto> aclDtoList = aclDao.getBySourceId(documentId);
        JsonArrayBuilder aclList = Json.createArrayBuilder();
        boolean writable = false;
        for (AclDto aclDto : aclDtoList) {
            aclList.add(Json.createObjectBuilder()
                    .add("perm", aclDto.getPerm().name())
                    .add("id", aclDto.getTargetId())
                    .add("name", JsonUtil.nullable(aclDto.getTargetName()))
                    .add("type", aclDto.getTargetType()));
            
            if (!principal.isAnonymous()
                    && aclDto.getTargetId().equals(principal.getId())
                    && aclDto.getPerm() == PermType.WRITE) {
                // The document is writable for the current user
                writable = true;
            }
        }
        document.add("acls", aclList)
                .add("writable", writable);
        
        return Response.ok().entity(document.build()).build();
    }
    
    /**
     * Returns all documents.
     * 
     * @param limit Page limit
     * @param offset Page offset
     * @return Response
     */
    @GET
    @Path("list")
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder documents = Json.createArrayBuilder();
        
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
            // Get tags added by the current user on this document
            List<TagDto> tagDtoList = tagDao.getByDocumentId(documentDto.getId(), principal.getId());
            JsonArrayBuilder tags = Json.createArrayBuilder();
            for (TagDto tagDto : tagDtoList) {
                tags.add(Json.createObjectBuilder()
                        .add("id", tagDto.getId())
                        .add("name", tagDto.getName())
                        .add("color", tagDto.getColor()));
            }
            
            documents.add(Json.createObjectBuilder()
                    .add("id", documentDto.getId())
                    .add("title", documentDto.getTitle())
                    .add("description", JsonUtil.nullable(documentDto.getDescription()))
                    .add("create_date", documentDto.getCreateTimestamp())
                    .add("language", documentDto.getLanguage())
                    .add("shared", documentDto.getShared())
                    .add("file_count", documentDto.getFileCount())
                    .add("tags", tags));
        }
        response.add("total", paginatedList.getResultCount())
                .add("documents", documents);
        
        return Response.ok().entity(response.build()).build();
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
                    // Invalid date, returns no documents
                    if (params[0].equals("before")) documentCriteria.setCreateDateMax(new Date(0));
                    else documentCriteria.setCreateDateMin(new Date(Long.MAX_VALUE / 2));
                }
            } else if (params[0].equals("at")) {
                // New specific date criteria
                try {
                    if (params[1].length() == 10) {
                        DateTime date = dayFormatter.parseDateTime(params[1]);
                        documentCriteria.setCreateDateMin(date.toDate());
                        documentCriteria.setCreateDateMax(date.plusDays(1).minusSeconds(1).toDate());
                    } else if (params[1].length() == 7) {
                        DateTime date = monthFormatter.parseDateTime(params[1]);
                        documentCriteria.setCreateDateMin(date.toDate());
                        documentCriteria.setCreateDateMax(date.plusMonths(1).minusSeconds(1).toDate());
                    } else if (params[1].length() == 4) {
                        DateTime date = yearFormatter.parseDateTime(params[1]);
                        documentCriteria.setCreateDateMin(date.toDate());
                        documentCriteria.setCreateDateMax(date.plusYears(1).minusSeconds(1).toDate());
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid date, returns no documents
                    documentCriteria.setCreateDateMin(new Date(0));
                    documentCriteria.setCreateDateMax(new Date(0));
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
     * @param tags Tags
     * @param language Language
     * @param createDateStr Creation date
     * @return Response
     */
    @PUT
    public Response add(
            @FormParam("title") String title,
            @FormParam("description") String description,
            @FormParam("tags") List<String> tagList,
            @FormParam("language") String language,
            @FormParam("create_date") String createDateStr) {
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
        
        // Create read ACL
        AclDao aclDao = new AclDao();
        Acl acl = new Acl();
        acl.setPerm(PermType.READ);
        acl.setSourceId(documentId);
        acl.setTargetId(principal.getId());
        aclDao.create(acl);
        
        // Create write ACL
        acl = new Acl();
        acl.setPerm(PermType.WRITE);
        acl.setSourceId(documentId);
        acl.setTargetId(principal.getId());
        aclDao.create(acl);
        
        // Update tags
        updateTagList(documentId, tagList);
        
        // Raise a document created event
        DocumentCreatedAsyncEvent documentCreatedAsyncEvent = new DocumentCreatedAsyncEvent();
        documentCreatedAsyncEvent.setDocument(document);
        AppContext.getInstance().getAsyncEventBus().post(documentCreatedAsyncEvent);
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", documentId);
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Updates the document.
     * 
     * @param title Title
     * @param description Description
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response update(
            @PathParam("id") String id,
            @FormParam("title") String title,
            @FormParam("description") String description,
            @FormParam("tags") List<String> tagList,
            @FormParam("language") String language,
            @FormParam("create_date") String createDateStr) {
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
        document = documentDao.getDocument(id, PermType.WRITE, principal.getId());
        if (document == null) {
            return Response.status(Status.NOT_FOUND).build();
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
        
        document = documentDao.update(document);
        
        // Update tags
        updateTagList(id, tagList);
        
        // Raise a document updated event
        DocumentUpdatedAsyncEvent documentUpdatedAsyncEvent = new DocumentUpdatedAsyncEvent();
        documentUpdatedAsyncEvent.setDocument(document);
        AppContext.getInstance().getAsyncEventBus().post(documentUpdatedAsyncEvent);
        
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Update tags list on a document.
     * 
     * @param documentId Document ID
     * @param tagList Tag ID list
     */
    private void updateTagList(String documentId, List<String> tagList) {
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
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(
            @PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the document
        DocumentDao documentDao = new DocumentDao();
        FileDao fileDao = new FileDao();
        Document document = documentDao.getDocument(id, PermType.WRITE, principal.getId());
        List<File> fileList = fileDao.getByDocumentId(principal.getId(), id);
        if (document == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Delete the document
        documentDao.delete(document.getId());
        
        // Raise file deleted events
        for (File file : fileList) {
            FileDeletedAsyncEvent fileDeletedAsyncEvent = new FileDeletedAsyncEvent();
            fileDeletedAsyncEvent.setFile(file);
            AppContext.getInstance().getAsyncEventBus().post(fileDeletedAsyncEvent);
        }
        
        // Raise a document deleted event
        DocumentDeletedAsyncEvent documentDeletedAsyncEvent = new DocumentDeletedAsyncEvent();
        documentDeletedAsyncEvent.setDocument(document);
        AppContext.getInstance().getAsyncEventBus().post(documentDeletedAsyncEvent);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}
