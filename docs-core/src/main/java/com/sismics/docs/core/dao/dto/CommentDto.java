package com.sismics.docs.core.dao.dto;

/**
 * Comment DTO.
 *
 * @author bgamard 
 */
public class CommentDto {
    /**
     * Comment ID.
     */
    private String id;
    
    /**
     * Creator name.
     */
    private String creatorName;
    
    /**
     * Creator email.
     */
    private String creatorEmail;
    
    /**
     * Content.
     */
    private String content;
    
    /**
     * Creation date of this comment.
     */
    private Long createTimestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    
    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }
}
