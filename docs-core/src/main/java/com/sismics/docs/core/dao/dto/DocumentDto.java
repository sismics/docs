package com.sismics.docs.core.dao.dto;

/**
 * Document DTO.
 *
 * @author bgamard 
 */
public class DocumentDto {
    /**
     * Document ID.
     */
    private String id;

    /**
     * Main file ID.
     */
    private String fileId;

    /**
     * Title.
     */
    private String title;
    
    /**
     * Description.
     */
    private String description;
    
    /**
     * Subject.
     */
    private String subject;
    
    /**
     * Identifier.
     */
    private String identifier;
    
    /**
     * Publisher.
     */
    private String publisher;
    
    /**
     * Format.
     */
    private String format;
    
    /**
     * Source.
     */
    private String source;
    
    /**
     * Type.
     */
    private String type;
    
    /**
     * Coverage.
     */
    private String coverage;
    
    /**
     * Rights.
     */
    private String rights;
    
    /**
     * Language.
     */
    private String language;
    
    /**
     * Creation date.
     */
    private Long createTimestamp;
    
    /**
     * Update date.
     */
    private Long updateTimestamp;

    /**
     * Shared status.
     */
    private Boolean shared;

    /**
     * File count.
     */
    private Integer fileCount;
    
    /**
     * Document creator.
     */
    private String creator;

    /**
     * A route is active.
     */
    private boolean activeRoute;

    /**
     * Current route step name.
     */
    private String currentStepName;

    /**
     * Search highlight.
     */
    private String highlight;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public DocumentDto setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getFileCount() {
        return fileCount;
    }

    public void setFileCount(Integer fileCount) {
        this.fileCount = fileCount;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean isActiveRoute() {
        return activeRoute;
    }

    public void setActiveRoute(boolean activeRoute) {
        this.activeRoute = activeRoute;
    }

    public String getCurrentStepName() {
        return currentStepName;
    }

    public Long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public DocumentDto setCurrentStepName(String currentStepName) {
        this.currentStepName = currentStepName;
        return this;
    }

    public String getHighlight() {
        return highlight;
    }

    public DocumentDto setHighlight(String highlight) {
        this.highlight = highlight;
        return this;
    }
}
