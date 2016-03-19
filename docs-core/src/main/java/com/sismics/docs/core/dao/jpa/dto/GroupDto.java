package com.sismics.docs.core.dao.jpa.dto;

/**
 * Group DTO.
 * 
 * @author bgamard
 */
public class GroupDto {
    /**
     * Group ID.
     */
    private String id;
    
    /**
     * Name.
     */
    private String name;

    /**
     * Parent ID.
     */
    private String parentId;
    
    /**
     * Parent name.
     */
    private String parentName;
    
    public String getId() {
        return id;
    }

    public GroupDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public GroupDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public GroupDto setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }
    
    public String getParentName() {
        return parentName;
    }

    public GroupDto setParentName(String parentName) {
        this.parentName = parentName;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return id.equals(((GroupDto) obj).getId());
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
