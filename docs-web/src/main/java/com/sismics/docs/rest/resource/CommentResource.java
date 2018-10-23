package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.AclDao;
import com.sismics.docs.core.dao.CommentDao;
import com.sismics.docs.core.dao.dto.CommentDto;
import com.sismics.docs.core.model.jpa.Comment;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.util.ImageUtil;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

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
     * @api {put} /comment Add a comment
     * @apiName PutComment
     * @apiGroup Comment
     * @apiParam {String} id Document ID
     * @apiParam {String} content Comment content
     * @apiSuccess {String} id Comment ID
     * @apiSuccess {String} content Content
     * @apiSuccess {String} creator Username
     * @apiSuccess {String} creator_gravatar Creator Gravatar hash
     * @apiSuccess {Number} create_date Create date (timestamp)
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) ValidationError Validation error
     * @apiError (client) NotFound Document not found
     * @apiPermission user
     * @apiVersion 1.5.0
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
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(null))) {
            throw new NotFoundException();
        }
        
        // Create the comment
        Comment comment = new Comment();
        comment.setDocumentId(documentId);
        comment.setContent(content);
        comment.setUserId(principal.getId());
        CommentDao commentDao = new CommentDao();
        commentDao.create(comment, principal.getId());
        
        // Returns the comment
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", comment.getId())
                .add("content", comment.getContent())
                .add("creator", principal.getName())
                .add("creator_gravatar", ImageUtil.computeGravatar(principal.getEmail()))
                .add("create_date", comment.getCreateDate().getTime());
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Delete a comment.
     *
     * @api {delete} /comment/:id Delete a comment
     * @apiName DeleteComment
     * @apiGroup Comment
     * @apiParam {String} id Comment ID
     * @apiSuccess {String} status Status OK
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Comment or document not found
     * @apiPermission user
     * @apiVersion 1.5.0
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
        
        // Get the comment
        CommentDao commentDao = new CommentDao();
        Comment comment = commentDao.getActiveById(id);
        if (comment == null) {
            throw new NotFoundException();
        }
        
        // If the current user owns the comment, skip ACL check
        if (!comment.getUserId().equals(principal.getId())) {
            // Get the associated document
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(comment.getDocumentId(), PermType.WRITE, getTargetIdList(null))) {
                throw new NotFoundException();
            }
        }
        
        // Delete the comment
        commentDao.delete(id, principal.getId());
        
        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
    
    /**
     * Get all comments on a document.
     *
     * @api {get} /comment/:id Get comments
     * @apiName GetComment
     * @apiGroup Comment
     * @apiParam {String} id Document ID
     * @apiParam {String} share Share ID
     * @apiSuccess {Object[]} comments List of comments
     * @apiSuccess {String} comments.id Comment ID
     * @apiSuccess {String} comments.content Content
     * @apiSuccess {String} comments.creator Username
     * @apiSuccess {String} comments.creator_gravatar Creator Gravatar hash
     * @apiSuccess {Number} comments.create_date Create date (timestamp)
     * @apiError (client) NotFound Document not found
     * @apiPermission none
     * @apiVersion 1.5.0
     *
     * @param documentId DocumentID
     * @return Response
     */
    @GET
    @Path("{documentId: [a-z0-9\\-]+}")
    public Response get(@PathParam("documentId") String documentId,
            @QueryParam("share") String shareId) {
        authenticate();
        
        // Read access on doc gives access to read comments 
        AclDao aclDao = new AclDao();
        if (!aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(shareId))) {
            throw new NotFoundException();
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
