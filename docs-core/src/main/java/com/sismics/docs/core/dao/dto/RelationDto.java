package com.sismics.docs.core.dao.dto;

/**
 * Tag DTO.
 *
 * @author bgamard 
 */
public class RelationDto {
    /**
     * Document ID.
     */
    private String id;
    
    /**
     * Document title.
     */
    private String title;

    /**
     * True if the document is the source of the relation.
     */
    private boolean source;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }
}
