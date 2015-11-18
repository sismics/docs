package com.sismics.docs.rest.resource;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.jpa.CommentDao;
import com.sismics.docs.core.dao.jpa.DocumentDao;
import com.sismics.docs.core.dao.jpa.dto.CommentDto;
import com.sismics.docs.core.model.jpa.Comment;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.ImageUtil;

/**
 * Comment REST resource.
 * 
 * @author bgamard
 */
@Path("/comment")
public class CommentResource extends BaseResource {
    /**
     * Add a comment.
     * 
     * @param documentId Document ID
     * @param content Comment content
     * @return Response
     */
    @PUT
    public Response add(@FormParam("id") String documentId,
            @FormParam("content") String content) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(documentId, "id");
        content = ValidationUtil.validateLength(content, "content", 1, 4000, false);
        
        // Read access on doc gives access to write comments 
        DocumentDao documentDao = new DocumentDao();
        if (documentDao.getDocument(documentId, PermType.READ, principal.getId()) == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Create the comment
        Comment comment = new Comment();
        comment.setDocumentId(documentId);
        comment.setContent(content);
        comment.setUserId(principal.getId());
        CommentDao commentDao = new CommentDao();
        commentDao.create(comment);
        
        // Returns the comment
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", comment.getId())
                .add("creator", principal.getName())
                .add("content", comment.getContent())
                .add("create_date", comment.getCreateDate().getTime());
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Delete a comment.
     * 
     * @param id Comment ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response delete(@PathParam("id") String id) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Validate input data
        ValidationUtil.validateRequired(id, "id");
        
        // Get the comment
        CommentDao commentDao = new CommentDao();
        Comment comment = commentDao.getActiveById(id);
        if (comment == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // If the current user owns the comment, skip ACL check
        if (!comment.getUserId().equals(principal.getId())) {
            // Get the associated document
            DocumentDao documentDao = new DocumentDao();
            if (documentDao.getDocument(comment.getDocumentId(), PermType.WRITE, principal.getId()) == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
        }
        
        // Delete the comment
        commentDao.delete(id);
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Get all comments on a document.
     * 
     * @param documentId DocumentID
     * @return Response
     */
    @GET
    @Path("{documentId: [a-z0-9\\-]+}")
    public Response get(@PathParam("documentId") String documentId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Read access on doc gives access to read comments 
        DocumentDao documentDao = new DocumentDao();
        if (documentDao.getDocument(documentId, PermType.READ, principal.getId()) == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        
        // Assemble results
        CommentDao commentDao = new CommentDao();
        List<CommentDto> commentDtoList = commentDao.getByDocumentId(documentId);
        JsonArrayBuilder comments = Json.createArrayBuilder();
        for (CommentDto commentDto : commentDtoList) {
            comments.add(Json.createObjectBuilder()
                    .add("id", commentDto.getId())
                    .add("content", commentDto.getContent())
                    .add("creator", commentDto.getCreatorName())
                    .add("creator_gravatar", ImageUtil.computeGravatar(commentDto.getCreatorEmail()))
                    .add("create_date", commentDto.getCreateTimestamp()));
        }
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("comments", comments);
        return Response.ok().entity(response.build()).build();
    }
}
