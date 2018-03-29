package com.sismics.docs.core.dao.dto;

/**
 * Tag DTO.
 *
 * @author bgamard 
 */
public class TagDto {
    /**
     * Tag ID.
     */
    private String id;
    
    /**
     * Name.
     */
    private String name;
    
    /**
     * Color.
     */
    private String color;
    
    /**
     * Parent ID.
     */
    private String parentId;

    /**
     * Creator.
     */
    private String creator;

    public String getId() {
        return id;
    }

    public TagDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TagDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getColor() {
        return color;
    }

    public TagDto setColor(String color) {
        this.color = color;
        return this;
    }
    
    public String getParentId() {
        return parentId;
    }

    public TagDto setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public TagDto setCreator(String creator) {
        this.creator = creator;
        return this;
    }
}
