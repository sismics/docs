package com.sismics.docs.rest.resource;

import com.google.common.collect.Lists;
import com.sismics.docs.core.constant.AclType;
import com.sismics.docs.core.constant.ConfigType;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.AclDao;
import com.sismics.docs.core.dao.ContributorDao;
import com.sismics.docs.core.dao.DocumentDao;
import com.sismics.docs.core.dao.FileDao;
import com.sismics.docs.core.dao.RelationDao;
import com.sismics.docs.core.dao.RouteStepDao;
import com.sismics.docs.core.dao.TagDao;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.criteria.TagCriteria;
import com.sismics.docs.core.dao.dto.AclDto;
import com.sismics.docs.core.dao.dto.ContributorDto;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.dao.dto.RelationDto;
import com.sismics.docs.core.dao.dto.RouteStepDto;
import com.sismics.docs.core.dao.dto.TagDto;
import com.sismics.docs.core.event.DocumentCreatedAsyncEvent;
import com.sismics.docs.core.event.DocumentDeletedAsyncEvent;
import com.sismics.docs.core.event.DocumentUpdatedAsyncEvent;
import com.sismics.docs.core.event.FileDeletedAsyncEvent;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.ConfigUtil;
import com.sismics.docs.core.util.DocumentUtil;
import com.sismics.docs.core.util.FileUtil;
import com.sismics.docs.core.util.MetadataUtil;
import com.sismics.docs.core.util.PdfUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.util.DocumentSearchCriteriaUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.AclUtil;
import com.sismics.rest.util.RestUtil;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.EmailUtil;
import com.sismics.util.JsonUtil;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.mime.MimeType;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

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
     * @api {get} /document/:id Get a document
     * @apiName GetDocument
     * @apiGroup Document
     * @apiParam {String} id Document ID
     * @apiParam {String} [share] Share ID
     * @apiParam {Boolean} [files] If true includes files information
     * @apiSuccess {String} id ID
     * @apiSuccess {String} title Title
     * @apiSuccess {String} description Description
     * @apiSuccess {Number} create_date Create date (timestamp)
     * @apiSuccess {Number} update_date Update date (timestamp)
     * @apiSuccess {String} language Language
     * @apiSuccess {Boolean} shared True if the document is shared
     * @apiSuccess {Number} file_count Number of files in this document
     * @apiSuccess {Object[]} tags List of tags
     * @apiSuccess {String} tags.id ID
     * @apiSuccess {String} tags.name Name
     * @apiSuccess {String} tags.color Color
     * @apiSuccess {String} subject Subject
     * @apiSuccess {String} identifier Identifier
     * @apiSuccess {String} publisher Publisher
     * @apiSuccess {String} format Format
     * @apiSuccess {String} source Source
     * @apiSuccess {String} type Type
     * @apiSuccess {String} coverage Coverage
     * @apiSuccess {String} rights Rights
     * @apiSuccess {String} creator Username of the creator
     * @apiSuccess {String} file_id Main file ID
     * @apiSuccess {Boolean} writable True if the document is writable by the current user
     * @apiSuccess {Object[]} acls List of ACL
     * @apiSuccess {String} acls.id ID
     * @apiSuccess {String="READ","WRITE"} acls.perm Permission
     * @apiSuccess {String} acls.name Target name
     * @apiSuccess {String="USER","GROUP","SHARE"} acls.type Target type
     * @apiSuccess {Object[]} inherited_acls List of ACL not directly applied to this document
     * @apiSuccess {String="READ","WRITE"} inherited_acls.perm Permission
     * @apiSuccess {String} inherited_acls.source_id Source ID
     * @apiSuccess {String} inherited_acls.source_name Source name
     * @apiSuccess {String} inherited_acls.source_color The color of the Source
     * @apiSuccess {String} inherited_acls.id ID
     * @apiSuccess {String} inherited_acls.name Target name
     * @apiSuccess {String="USER","GROUP","SHARE"} inherited_acls.type Target type
     * @apiSuccess {Object[]} contributors List of users having contributed to this document
     * @apiSuccess {String} contributors.username Username
     * @apiSuccess {String} contributors.email E-mail
     * @apiSuccess {Object[]} relations List of document related to this one
     * @apiSuccess {String} relations.id ID
     * @apiSuccess {String} relations.title Title
     * @apiSuccess {String} relations.source True if this document is the source of the relation
     * @apiSuccess {Object} route_step The current active route step
     * @apiSuccess {String} route_step.name Route step name
     * @apiSuccess {String="APPROVE", "VALIDATE"} route_step.type Route step type
     * @apiSuccess {Boolean} route_step.transitionable True if the route step is actionable by the current user
     * @apiSuccess {Object[]} files List of files
     * @apiSuccess {String} files.id ID
     * @apiSuccess {String} files.name File name
     * @apiSuccess {String} files.version Zero-based version number
     * @apiSuccess {String} files.mimetype MIME type
     * @apiSuccess {String} files.create_date Create date (timestamp)
     * @apiSuccess {Object[]} metadata List of metadata
     * @apiSuccess {String} metadata.id ID
     * @apiSuccess {String} metadata.name Name
     * @apiSuccess {String="STRING","INTEGER","FLOAT","DATE","BOOLEAN"} metadata.type Type
     * @apiSuccess {Object} metadata.value Value
     * @apiError (client) NotFound Document not found
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param documentId Document ID
     * @param shareId Share ID
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    public Response get(
            @PathParam("id") String documentId,
            @QueryParam("share") String shareId,
            @QueryParam("files") Boolean files) {
        authenticate();

        DocumentDao documentDao = new DocumentDao();
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, getTargetIdList(shareId));
        if (documentDto == null) {
            throw new NotFoundException();
        }

        JsonObjectBuilder document = createDocumentObjectBuilder(documentDto)
                .add("creator", documentDto.getCreator())
                .add("coverage", JsonUtil.nullable(documentDto.getCoverage()))
                .add("file_count", documentDto.getFileCount())
                .add("format", JsonUtil.nullable(documentDto.getFormat()))
                .add("identifier", JsonUtil.nullable(documentDto.getIdentifier()))
                .add("publisher", JsonUtil.nullable(documentDto.getPublisher()))
                .add("rights", JsonUtil.nullable(documentDto.getRights()))
                .add("source", JsonUtil.nullable(documentDto.getSource()))
                .add("subject", JsonUtil.nullable(documentDto.getSubject()))
                .add("type", JsonUtil.nullable(documentDto.getType()));

        List<TagDto> tagDtoList = null;
        if (principal.isAnonymous()) {
            // No tags in anonymous mode (sharing)
            document.add("tags", Json.createArrayBuilder());
        } else {
            // Add tags visible by the current user on this document
            TagDao tagDao = new TagDao();
            tagDtoList = tagDao.findByCriteria(
                    new TagCriteria()
                            .setTargetIdList(getTargetIdList(null)) // No tags for shares
                            .setDocumentId(documentId),
                    new SortCriteria(1, true));
            document.add("tags", createTagsArrayBuilder(tagDtoList));
        }

        // Add ACL
        AclUtil.addAcls(document, documentId, getTargetIdList(shareId));

        // Add computed ACL
        if (tagDtoList != null) {
            JsonArrayBuilder aclList = Json.createArrayBuilder();
            for (TagDto tagDto : tagDtoList) {
                AclDao aclDao = new AclDao();
                List<AclDto> aclDtoList = aclDao.getBySourceId(tagDto.getId(), AclType.USER);
                for (AclDto aclDto : aclDtoList) {
                    aclList.add(Json.createObjectBuilder()
                            .add("perm", aclDto.getPerm().name())
                            .add("source_id", tagDto.getId())
                            .add("source_name", tagDto.getName())
                            .add("source_color", tagDto.getColor())
                            .add("id", aclDto.getTargetId())
                            .add("name", JsonUtil.nullable(aclDto.getTargetName()))
                            .add("type", aclDto.getTargetType()));
                }
            }
            document.add("inherited_acls", aclList);
        }

        // Add contributors
        ContributorDao contributorDao = new ContributorDao();
        List<ContributorDto> contributorDtoList = contributorDao.getByDocumentId(documentId);
        JsonArrayBuilder contributorList = Json.createArrayBuilder();
        for (ContributorDto contributorDto : contributorDtoList) {
            contributorList.add(Json.createObjectBuilder()
                    .add("username", contributorDto.getUsername())
                    .add("email", contributorDto.getEmail()));
        }
        document.add("contributors", contributorList);

        // Add relations
        RelationDao relationDao = new RelationDao();
        List<RelationDto> relationDtoList = relationDao.getByDocumentId(documentId);
        JsonArrayBuilder relationList = Json.createArrayBuilder();
        for (RelationDto relationDto : relationDtoList) {
            relationList.add(Json.createObjectBuilder()
                    .add("id", relationDto.getId())
                    .add("title", relationDto.getTitle())
                    .add("source", relationDto.isSource()));
        }
        document.add("relations", relationList);

        // Add current route step
        RouteStepDto routeStepDto = new RouteStepDao().getCurrentStep(documentId);
        if (routeStepDto != null && !principal.isAnonymous()) {
            JsonObjectBuilder step = routeStepDto.toJson();
            step.add("transitionable", getTargetIdList(null).contains(routeStepDto.getTargetId()));
            document.add("route_step", step);
        }

        // Add custom metadata
        MetadataUtil.addMetadata(document, documentId);

        // Add files
        if (Boolean.TRUE == files) {
            FileDao fileDao = new FileDao();
            List<File> fileList = fileDao.getByDocumentsIds(Collections.singleton(documentId));

            JsonArrayBuilder filesArrayBuilder = Json.createArrayBuilder();
            for (File fileDb : fileList) {
                filesArrayBuilder.add(RestUtil.fileToJsonObjectBuilder(fileDb));
            }

            document.add("files", filesArrayBuilder);
        }

        return Response.ok().entity(document.build()).build();
    }

    /**
     * Export a document to PDF.
     *
     * @api {get} /document/:id/pdf Export a document to PDF
     * @apiName GetDocumentPdf
     * @apiGroup Document
     * @apiParam {String} id Document ID
     * @apiParam {String} share Share ID
     * @apiParam {Boolean} metadata If true, export metadata
     * @apiParam {Boolean} fitimagetopage If true, fit the images to pages
     * @apiParam {Number} margin Margin around the pages, in millimeter
     * @apiSuccess {String} pdf The whole response is the PDF file
     * @apiError (client) NotFound Document not found
     * @apiError (client) ValidationError Validation error
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param documentId Document ID
     * @param shareId Share ID
     * @param metadata Export metadata
     * @param fitImageToPage Fit images to page
     * @param marginStr Margins
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}/pdf")
    public Response getPdf(
            @PathParam("id") String documentId,
            @QueryParam("share") String shareId,
            final @QueryParam("metadata") Boolean metadata,
            final @QueryParam("fitimagetopage") Boolean fitImageToPage,
            @QueryParam("margin") String marginStr) {
        authenticate();

        // Validate input
        final int margin = ValidationUtil.validateInteger(marginStr, "margin");

        // Get document and check read permission
        DocumentDao documentDao = new DocumentDao();
        final DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, getTargetIdList(shareId));
        if (documentDto == null) {
            throw new NotFoundException();
        }

        // Get files
        FileDao fileDao = new FileDao();
        UserDao userDao = new UserDao();
        final List<File> fileList = fileDao.getByDocumentId(null, documentId);
        for (File file : fileList) {
            // A file is always encrypted by the creator of it
            // Store its private key to decrypt it
            User user = userDao.getById(file.getUserId());
            file.setPrivateKey(user.getPrivateKey());
        }

        // Convert to PDF
        StreamingOutput stream = outputStream -> {
            try {
                PdfUtil.convertToPdf(documentDto, fileList, fitImageToPage, metadata, margin, outputStream);
            } catch (Exception e) {
                throw new IOException(e);
            }
        };

        return Response.ok(stream)
                .header("Content-Type", MimeType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"" + documentDto.getTitle() + ".pdf\"")
                .build();
    }

    /**
     * Returns all documents, if a parameter is considered invalid, the search result will be empty.
     *
     * @api {get} /document/list Get documents
     * @apiName GetDocumentList
     * @apiGroup Document
     *
     * @apiParam {String} [limit] Total number of documents to return (default is <code>10</code>)
     * @apiParam {String} [offset] Start at this index (default is <code>0</code>)
     * @apiParam {Number} [sort_column] Column index to sort on
     * @apiParam {Boolean} [asc] If <code>true</code> sorts in ascending order
     * @apiParam {String} [search] Search query (see "Document search syntax" on the top of the page for explanations) when the input is entered by a human.
     * @apiParam {Boolean} [files] If <code>true</code> includes files information
     *
     * @apiParam {String} [search[after]] The document must have been created after or at the value moment, accepted format is <code>yyyy-MM-dd</code>
     * @apiParam {String} [search[before]] The document must have been created before or at the value moment, accepted format is <code>yyyy-MM-dd</code>
     * @apiParam {String} [search[by]] The document must have been created by the specified creator's username with an exact match, the user must not be deleted
     * @apiParam {String} [search[full]] Used as a search criteria for all fields including the document's files content, several comma-separated values can be specified and the document must match any of them
     * @apiParam {String} [search[lang]] The document must be of the specified language (example: <code>en</code>)
     * @apiParam {String} [search[mime]] The document must be of the specified mime type (example: <code>image/png</code>)
     * @apiParam {String} [search[simple]] Used as a search criteria for all fields except the document's files content, several comma-separated values can be specified and the document must match any of them
     * @apiParam {Boolean} [search[shared]] If <code>true</code> the document must be shared, else it is ignored
     * @apiParam {String} [search[tag]] The document must contain a tag or a child of a tag that starts with the value, case is ignored, several comma-separated values can be specified and the document must match all tag filters
     * @apiParam {String} [search[nottag]] The document must not contain a tag or a child of a tag that starts with the value, case is ignored, several comma-separated values can be specified and the document must match all tag filters
     * @apiParam {String} [search[title]] The document's title must be the value, several comma-separated values can be specified and the document must match any of the titles
     * @apiParam {String} [search[uafter]] The document must have been updated after or at the value moment, accepted format is <code>yyyy-MM-dd</code>
     * @apiParam {String} [search[ubefore]] The document must have been updated before or at the value moment, accepted format is <code>yyyy-MM-dd</code>
     * @apiParam {String} [search[workflow]] If the value is <code>me</code> the document must have an active route, for other values the criteria is ignored
     *
     * @apiSuccess {Number} total Total number of documents
     * @apiSuccess {Object[]} documents List of documents
     * @apiSuccess {String} documents.id ID
     * @apiSuccess {String} documents.highlight Search highlight (for fulltext search)
     * @apiSuccess {String} documents.file_id Main file ID
     * @apiSuccess {String} documents.title Title
     * @apiSuccess {String} documents.description Description
     * @apiSuccess {Number} documents.create_date Create date (timestamp)
     * @apiSuccess {Number} documents.update_date Update date (timestamp)
     * @apiSuccess {String} documents.language Language
     * @apiSuccess {Boolean} documents.shared True if the document is shared
     * @apiSuccess {Boolean} documents.active_route True if a route is active on this document
     * @apiSuccess {Boolean} documents.current_step_name Name of the current route step
     * @apiSuccess {Number} documents.file_count Number of files in this document
     * @apiSuccess {Object[]} documents.tags List of tags
     * @apiSuccess {String} documents.tags.id ID
     * @apiSuccess {String} documents.tags.name Name
     * @apiSuccess {String} documents.tags.color Color
     * @apiSuccess {Object[]} documents.files List of files
     * @apiSuccess {String} documents.files.id ID
     * @apiSuccess {String} documents.files.name File name
     * @apiSuccess {String} documents.files.version Zero-based version number
     * @apiSuccess {String} documents.files.mimetype MIME type
     * @apiSuccess {String} documents.files.create_date Create date (timestamp)
     * @apiSuccess {String[]} suggestions List of search suggestions
     *
     * @apiError (client) ForbiddenError Access denied
     * @apiError (server) SearchError Error searching in documents
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param limit Page limit
     * @param offset Page offset
     * @param sortColumn Sort column
     * @param asc Sorting
     * @param search Search query
     * @param files Files list
     * @return Response
     */
    @GET
    @Path("list")
    public Response list(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc,
            @QueryParam("search") String search,
            @QueryParam("files") Boolean files,

            @QueryParam("search[after]") String searchCreatedAfter,
            @QueryParam("search[before]") String searchCreatedBefore,
            @QueryParam("search[by]") String searchBy,
            @QueryParam("search[full]") String searchFull,
            @QueryParam("search[lang]") String searchLang,
            @QueryParam("search[mime]") String searchMime,
            @QueryParam("search[shared]") Boolean searchShared,
            @QueryParam("search[simple]") String searchSimple,
            @QueryParam("search[tag]") String searchTag,
            @QueryParam("search[nottag]") String searchTagNot,
            @QueryParam("search[title]") String searchTitle,
            @QueryParam("search[uafter]") String searchUpdatedAfter,
            @QueryParam("search[ubefore]") String searchUpdatedBefore,
            @QueryParam("search[searchworkflow]") String searchWorkflow
    ) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder documents = Json.createArrayBuilder();

        TagDao tagDao = new TagDao();
        PaginatedList<DocumentDto> paginatedList = PaginatedLists.create(limit, offset);
        List<String> suggestionList = Lists.newArrayList();
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);

        List<TagDto> allTagDtoList = tagDao.findByCriteria(new TagCriteria().setTargetIdList(getTargetIdList(null)), null);

        DocumentCriteria documentCriteria = DocumentSearchCriteriaUtil.parseSearchQuery(search, allTagDtoList);
        DocumentSearchCriteriaUtil.addHttpSearchParams(
                documentCriteria,
                searchBy,
                searchCreatedAfter,
                searchCreatedBefore,
                searchFull,
                searchLang,
                searchMime,
                searchShared,
                searchSimple,
                searchTag,
                searchTagNot,
                searchTitle,
                searchUpdatedAfter,
                searchUpdatedBefore,
                searchWorkflow,
                allTagDtoList);

        documentCriteria.setTargetIdList(getTargetIdList(null));
        try {
            AppContext.getInstance().getIndexingHandler().findByCriteria(paginatedList, suggestionList, documentCriteria, sortCriteria);
        } catch (Exception e) {
            throw new ServerException("SearchError", "Error searching in documents", e);
        }

        // Find the files of the documents
        Iterable<String> documentsIds = CollectionUtils.collect(paginatedList.getResultList(), DocumentDto::getId);
        FileDao fileDao = new FileDao();
        List<File> filesList = null;
        Map<String, Long> filesCountByDocument = null;
        if (Boolean.TRUE == files) {
            filesList = fileDao.getByDocumentsIds(documentsIds);
        } else {
            filesCountByDocument = fileDao.countByDocumentsIds(documentsIds);
        }

        for (DocumentDto documentDto : paginatedList.getResultList()) {
            // Get tags accessible by the current user on this document
            List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria()
                    .setTargetIdList(getTargetIdList(null))
                    .setDocumentId(documentDto.getId()), new SortCriteria(1, true));

            Long filesCount;
            Collection<File> filesOfDocument = null;
            if (Boolean.TRUE == files) {
                // Find files matching the document
                filesOfDocument = CollectionUtils.select(filesList, file -> file.getDocumentId().equals(documentDto.getId()));
                filesCount = (long) filesOfDocument.size();
            } else {
                filesCount = filesCountByDocument.getOrDefault(documentDto.getId(), 0L);
            }

            JsonObjectBuilder documentObjectBuilder = createDocumentObjectBuilder(documentDto)
                    .add("active_route", documentDto.isActiveRoute())
                    .add("current_step_name", JsonUtil.nullable(documentDto.getCurrentStepName()))
                    .add("highlight", JsonUtil.nullable(documentDto.getHighlight()))
                    .add("file_count", filesCount)
                    .add("tags", createTagsArrayBuilder(tagDtoList));

            if (Boolean.TRUE == files) {
                JsonArrayBuilder filesArrayBuilder = Json.createArrayBuilder();
                for (File fileDb : filesOfDocument) {
                    filesArrayBuilder.add(RestUtil.fileToJsonObjectBuilder(fileDb));
                }
                documentObjectBuilder.add("files", filesArrayBuilder);
            }
            documents.add(documentObjectBuilder);
        }

        JsonArrayBuilder suggestions = Json.createArrayBuilder();
        for (String suggestion : suggestionList) {
            suggestions.add(suggestion);
        }

        response.add("total", paginatedList.getResultCount())
                .add("documents", documents)
                .add("suggestions", suggestions);

        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns all documents.
     *
     * @api {post} /document/list Get documents
     * @apiDescription Get documents exposed as a POST endpoint to allow longer search parameters, see the GET endpoint for the API info
     * @apiName PostDocumentList
     * @apiGroup Document
     * @apiVersion 1.12.0
     *
     * @param limit      Page limit
     * @param offset     Page offset
     * @param sortColumn Sort column
     * @param asc        Sorting
     * @param search     Search query
     * @param files      Files list
     * @return Response
     */
    @POST
    @Path("list")
    public Response listPost(
            @FormParam("limit") Integer limit,
            @FormParam("offset") Integer offset,
            @FormParam("sort_column") Integer sortColumn,
            @FormParam("asc") Boolean asc,
            @FormParam("search") String search,
            @FormParam("files") Boolean files,
            @FormParam("search[after]") String searchCreatedAfter,
            @FormParam("search[before]") String searchCreatedBefore,
            @FormParam("search[by]") String searchBy,
            @FormParam("search[full]") String searchFull,
            @FormParam("search[lang]") String searchLang,
            @FormParam("search[mime]") String searchMime,
            @FormParam("search[shared]") Boolean searchShared,
            @FormParam("search[simple]") String searchSimple,
            @FormParam("search[tag]") String searchTag,
            @FormParam("search[nottag]") String searchTagNot,
            @FormParam("search[title]") String searchTitle,
            @FormParam("search[uafter]") String searchUpdatedAfter,
            @FormParam("search[ubefore]") String searchUpdatedBefore,
            @FormParam("search[searchworkflow]") String searchWorkflow
    ) {
        return list(
                limit,
                offset,
                sortColumn,
                asc,
                search,
                files,
                searchCreatedAfter,
                searchCreatedBefore,
                searchBy,
                searchFull,
                searchLang,
                searchMime,
                searchShared,
                searchSimple,
                searchTag,
                searchTagNot,
                searchTitle,
                searchUpdatedAfter,
                searchUpdatedBefore,
                searchWorkflow
        );
    }

    /**
     * Creates a new document.
     *
     * @api {put} /document Add a document
     * @apiName PutDocument
     * @apiGroup Document
     * @apiParam {String} title Title
     * @apiParam {String} [description] Description
     * @apiParam {String} [subject] Subject
     * @apiParam {String} [identifier] Identifier
     * @apiParam {String} [publisher] Publisher
     * @apiParam {String} [format] Format
     * @apiParam {String} [source] Source
     * @apiParam {String} [type] Type
     * @apiParam {String} [coverage] Coverage
     * @apiParam {String} [rights] Rights
     * @apiParam {String[]} [tags] List of tags ID
     * @apiParam {String[]} [relations] List of related documents ID
     * @apiParam {String[]} [metadata_id] List of metadata ID
     * @apiParam {String[]} [metadata_value] List of metadata values
     * @apiParam {String} language Language
     * @apiParam {Number} [create_date] Create date (timestamp)
     * @apiSuccess {String} id Document ID
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param title Title
     * @param description Description
     * @param subject Subject
     * @param identifier Identifier
     * @param publisher Publisher
     * @param format Format
     * @param source Source
     * @param type Type
     * @param coverage Coverage
     * @param rights Rights
     * @param tagList Tags
     * @param relationList Relations
     * @param metadataIdList Metadata ID list
     * @param metadataValueList Metadata value list
     * @param language Language
     * @param createDateStr Creation date
     * @return Response
     */
    @PUT
    public Response add(
            @FormParam("title") String title,
            @FormParam("description") String description,
            @FormParam("subject") String subject,
            @FormParam("identifier") String identifier,
            @FormParam("publisher") String publisher,
            @FormParam("format") String format,
            @FormParam("source") String source,
            @FormParam("type") String type,
            @FormParam("coverage") String coverage,
            @FormParam("rights") String rights,
            @FormParam("tags") List<String> tagList,
            @FormParam("relations") List<String> relationList,
            @FormParam("metadata_id") List<String> metadataIdList,
            @FormParam("metadata_value") List<String> metadataValueList,
            @FormParam("language") String language,
            @FormParam("create_date") String createDateStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        title = ValidationUtil.validateLength(title, "title", 1, 100, false);
        language = ValidationUtil.validateLength(language, "language", 3, 7, false);
        description = ValidationUtil.validateLength(description, "description", 0, 4000, true);
        subject = ValidationUtil.validateLength(subject, "subject", 0, 500, true);
        identifier = ValidationUtil.validateLength(identifier, "identifier", 0, 500, true);
        publisher = ValidationUtil.validateLength(publisher, "publisher", 0, 500, true);
        format = ValidationUtil.validateLength(format, "format", 0, 500, true);
        source = ValidationUtil.validateLength(source, "source", 0, 500, true);
        type = ValidationUtil.validateLength(type, "type", 0, 100, true);
        coverage = ValidationUtil.validateLength(coverage, "coverage", 0, 100, true);
        rights = ValidationUtil.validateLength(rights, "rights", 0, 100, true);
        Date createDate = ValidationUtil.validateDate(createDateStr, "create_date", true);
        if (!Constants.SUPPORTED_LANGUAGES.contains(language)) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} is not a supported language", language));
        }

        // Create the document
        Document document = new Document();
        document.setUserId(principal.getId());
        document.setTitle(title);
        document.setDescription(description);
        document.setSubject(subject);
        document.setIdentifier(identifier);
        document.setPublisher(publisher);
        document.setFormat(format);
        document.setSource(source);
        document.setType(type);
        document.setCoverage(coverage);
        document.setRights(rights);
        document.setLanguage(language);
        if (createDate == null) {
            document.setCreateDate(new Date());
        } else {
            document.setCreateDate(createDate);
        }

        // Save the document, create the base ACLs
        document = DocumentUtil.createDocument(document, principal.getId());

        // Update tags
        updateTagList(document.getId(), tagList);

        // Update relations
        updateRelationList(document.getId(), relationList);

        // Update custom metadata
        try {
            MetadataUtil.updateMetadata(document.getId(), metadataIdList, metadataValueList);
        } catch (Exception e) {
            throw new ClientException("ValidationError", e.getMessage());
        }

        // Raise a document created event
        DocumentCreatedAsyncEvent documentCreatedAsyncEvent = new DocumentCreatedAsyncEvent();
        documentCreatedAsyncEvent.setUserId(principal.getId());
        documentCreatedAsyncEvent.setDocumentId(document.getId());
        ThreadLocalContext.get().addAsyncEvent(documentCreatedAsyncEvent);

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", document.getId());
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Updates the document.
     *
     * @api {post} /document/:id Update a document
     * @apiName PostDocument
     * @apiGroup Document
     * @apiParam {String} id ID
     * @apiParam {String} title Title
     * @apiParam {String} [description] Description
     * @apiParam {String} [subject] Subject
     * @apiParam {String} [identifier] Identifier
     * @apiParam {String} [publisher] Publisher
     * @apiParam {String} [format] Format
     * @apiParam {String} [source] Source
     * @apiParam {String} [type] Type
     * @apiParam {String} [coverage] Coverage
     * @apiParam {String} [rights] Rights
     * @apiParam {String[]} [tags] List of tags ID
     * @apiParam {String[]} [relations] List of related documents ID
     * @apiParam {String[]} [metadata_id] List of metadata ID
     * @apiParam {String[]} [metadata_value] List of metadata values
     * @apiParam {String} [language] Language
     * @apiParam {Number} [create_date] Create date (timestamp)
     * @apiSuccess {String} id Document ID
     * @apiError (client) ForbiddenError Access denied or document not writable
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Document not found
     * @apiPermission user
     * @apiVersion 1.5.0
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
            @FormParam("subject") String subject,
            @FormParam("identifier") String identifier,
            @FormParam("publisher") String publisher,
            @FormParam("format") String format,
            @FormParam("source") String source,
            @FormParam("type") String type,
            @FormParam("coverage") String coverage,
            @FormParam("rights") String rights,
            @FormParam("tags") List<String> tagList,
            @FormParam("relations") List<String> relationList,
            @FormParam("metadata_id") List<String> metadataIdList,
            @FormParam("metadata_value") List<String> metadataValueList,
            @FormParam("language") String language,
            @FormParam("create_date") String createDateStr) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        title = ValidationUtil.validateLength(title, "title", 1, 100, false);
        language = ValidationUtil.validateLength(language, "language", 3, 7, false);
        description = ValidationUtil.validateLength(description, "description", 0, 4000, true);
        subject = ValidationUtil.validateLength(subject, "subject", 0, 500, true);
        identifier = ValidationUtil.validateLength(identifier, "identifier", 0, 500, true);
        publisher = ValidationUtil.validateLength(publisher, "publisher", 0, 500, true);
        format = ValidationUtil.validateLength(format, "format", 0, 500, true);
        source = ValidationUtil.validateLength(source, "source", 0, 500, true);
        type = ValidationUtil.validateLength(type, "type", 0, 100, true);
        coverage = ValidationUtil.validateLength(coverage, "coverage", 0, 100, true);
        rights = ValidationUtil.validateLength(rights, "rights", 0, 100, true);
        Date createDate = ValidationUtil.validateDate(createDateStr, "create_date", true);
        if (language != null && !Constants.SUPPORTED_LANGUAGES.contains(language)) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} is not a supported language", language));
        }

        // Check write permission
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(id, PermType.WRITE, getTargetIdList(null))) {
            throw new ForbiddenClientException();
        }

        // Get the document
        DocumentDao documentDao = new DocumentDao();
        Document document = documentDao.getById(id);
        if (document == null) {
            throw new NotFoundException();
        }

        // Update the document
        document.setTitle(title);
        document.setDescription(description);
        document.setSubject(subject);
        document.setIdentifier(identifier);
        document.setPublisher(publisher);
        document.setFormat(format);
        document.setSource(source);
        document.setType(type);
        document.setCoverage(coverage);
        document.setRights(rights);
        document.setLanguage(language);
        if (createDate == null) {
            document.setCreateDate(new Date());
        } else {
            document.setCreateDate(createDate);
        }

        documentDao.update(document, principal.getId());

        // Update tags
        updateTagList(id, tagList);

        // Update relations
        updateRelationList(id, relationList);

        // Update custom metadata
        try {
            MetadataUtil.updateMetadata(document.getId(), metadataIdList, metadataValueList);
        } catch (Exception e) {
            throw new ClientException("ValidationError", e.getMessage());
        }

        // Raise a document updated event
        DocumentUpdatedAsyncEvent documentUpdatedAsyncEvent = new DocumentUpdatedAsyncEvent();
        documentUpdatedAsyncEvent.setUserId(principal.getId());
        documentUpdatedAsyncEvent.setDocumentId(id);
        ThreadLocalContext.get().addAsyncEvent(documentUpdatedAsyncEvent);

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Import a new document from an EML file.
     *
     * @api {put} /document/eml Import a new document from an EML file
     * @apiName PutDocumentEml
     * @apiGroup Document
     * @apiParam {String} file File data
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (server) StreamError Error reading the input file
     * @apiError (server) ErrorGuessMime Error guessing mime type
     * @apiError (client) QuotaReached Quota limit reached
     * @apiError (server) FileError Error adding a file
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @param fileBodyPart File to import
     * @return Response
     */
    @PUT
    @Path("eml")
    @Consumes("multipart/form-data")
    public Response importEml(@FormDataParam("file") FormDataBodyPart fileBodyPart) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Validate input data
        ValidationUtil.validateRequired(fileBodyPart, "file");

        // Save the file to a temporary file
        java.nio.file.Path unencryptedFile;
        try {
            unencryptedFile = AppContext.getInstance().getFileService().createTemporaryFile();
            Files.copy(fileBodyPart.getValueAs(InputStream.class), unencryptedFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ServerException("StreamError", "Error reading the input file", e);
        }

        // Read the EML file
        Properties props = new Properties();
        Session mailSession = Session.getDefaultInstance(props, null);
        EmailUtil.MailContent mailContent = new EmailUtil.MailContent();
        try (InputStream inputStream = Files.newInputStream(unencryptedFile)) {
            Message message = new MimeMessage(mailSession, inputStream);
            mailContent.setSubject(message.getSubject());
            mailContent.setDate(message.getSentDate());
            EmailUtil.parseMailContent(message, mailContent);
        } catch (IOException | MessagingException e) {
            throw new ServerException("StreamError", "Error reading the temporary file", e);
        }

        // Create the document
        Document document = new Document();
        document.setUserId(principal.getId());
        if (mailContent.getSubject() == null) {
            document.setTitle("Imported email from EML file");
        } else {
            document.setTitle(StringUtils.abbreviate(mailContent.getSubject(), 100));
        }
        document.setDescription(StringUtils.abbreviate(mailContent.getMessage(), 4000));
        document.setSubject(StringUtils.abbreviate(mailContent.getSubject(), 500));
        document.setFormat("EML");
        document.setSource("Email");
        document.setLanguage(ConfigUtil.getConfigStringValue(ConfigType.DEFAULT_LANGUAGE));
        if (mailContent.getDate() == null) {
            document.setCreateDate(new Date());
        } else {
            document.setCreateDate(mailContent.getDate());
        }

        // Save the document, create the base ACLs
        DocumentUtil.createDocument(document, principal.getId());

        // Raise a document created event
        DocumentCreatedAsyncEvent documentCreatedAsyncEvent = new DocumentCreatedAsyncEvent();
        documentCreatedAsyncEvent.setUserId(principal.getId());
        documentCreatedAsyncEvent.setDocumentId(document.getId());
        ThreadLocalContext.get().addAsyncEvent(documentCreatedAsyncEvent);

        // Add files to the document
        try {
            for (EmailUtil.FileContent fileContent : mailContent.getFileContentList()) {
                FileUtil.createFile(fileContent.getName(), null, fileContent.getFile(), fileContent.getSize(),
                        document.getLanguage(), principal.getId(), document.getId());
            }
        } catch (IOException e) {
            throw new ClientException(e.getMessage(), e.getMessage(), e);
        } catch (Exception e) {
            throw new ServerException("FileError", "Error adding a file", e);
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", document.getId());
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Deletes a document.
     *
     * @api {delete} /document/:id Delete a document
     * @apiName DeleteDocument
     * @apiGroup Document
     * @apiParam {String} id ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Document not found
     * @apiPermission user
     * @apiVersion 1.5.0
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
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(id, PermType.WRITE, getTargetIdList(null))) {
            throw new NotFoundException();
        }
        List<File> fileList = fileDao.getByDocumentId(principal.getId(), id);

        // Delete the document
        documentDao.delete(id, principal.getId());

        for (File file : fileList) {
            // Raise file deleted event
            FileDeletedAsyncEvent fileDeletedAsyncEvent = new FileDeletedAsyncEvent();
            fileDeletedAsyncEvent.setUserId(principal.getId());
            fileDeletedAsyncEvent.setFileId(file.getId());
            fileDeletedAsyncEvent.setFileSize(file.getSize());
            ThreadLocalContext.get().addAsyncEvent(fileDeletedAsyncEvent);
        }

        // Raise a document deleted event
        DocumentDeletedAsyncEvent documentDeletedAsyncEvent = new DocumentDeletedAsyncEvent();
        documentDeletedAsyncEvent.setUserId(principal.getId());
        documentDeletedAsyncEvent.setDocumentId(id);
        ThreadLocalContext.get().addAsyncEvent(documentDeletedAsyncEvent);

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
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
            List<TagDto> tagDtoList = tagDao.findByCriteria(new TagCriteria().setTargetIdList(getTargetIdList(null)), null);
            for (TagDto tagDto : tagDtoList) {
                tagIdSet.add(tagDto.getId());
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
     * Update relations list on a document.
     *
     * @param documentId Document ID
     * @param relationList Relation ID list
     */
    private void updateRelationList(String documentId, List<String> relationList) {
        if (relationList != null) {
            DocumentDao documentDao = new DocumentDao();
            RelationDao relationDao = new RelationDao();
            Set<String> documentIdSet = new HashSet<>();
            for (String targetDocId : relationList) {
                // ACL are not checked, because the editing user is not forced to view the target document
                Document document = documentDao.getById(targetDocId);
                if (document != null && !documentId.equals(targetDocId)) {
                    documentIdSet.add(targetDocId);
                }
            }
            relationDao.updateRelationList(documentId, documentIdSet);
        }
    }

    private JsonObjectBuilder createDocumentObjectBuilder(DocumentDto documentDto) {
        return Json.createObjectBuilder()
                .add("create_date", documentDto.getCreateTimestamp())
                .add("description", JsonUtil.nullable(documentDto.getDescription()))
                .add("file_id", JsonUtil.nullable(documentDto.getFileId()))
                .add("id", documentDto.getId())
                .add("language", documentDto.getLanguage())
                .add("shared", documentDto.getShared())
                .add("title", documentDto.getTitle())
                .add("update_date", documentDto.getUpdateTimestamp());
    }

    private static JsonArrayBuilder createTagsArrayBuilder(List<TagDto> tagDtoList) {
        JsonArrayBuilder tags = Json.createArrayBuilder();
        for (TagDto tagDto : tagDtoList) {
            tags.add(Json.createObjectBuilder()
                    .add("id", tagDto.getId())
                    .add("name", tagDto.getName())
                    .add("color", tagDto.getColor()));
        }
        return tags;
    }
}
