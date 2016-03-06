package com.sismics.docs.core.dao.jpa.dto;

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
}
