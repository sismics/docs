package com.sismics.docs.core.dao.dto;

/**
 * Route model DTO.
 *
 * @author bgamard 
 */
public class RouteModelDto {
    /**
     * Route model ID.
     */
    private String id;
    
    /**
     * Name.
     */
    private String name;
    
    /**
     * Creation date.
     */
    private Long createTimestamp;

    public String getId() {
        return id;
    }

    public RouteModelDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public RouteModelDto setName(String name) {
        this.name = name;
        return this;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public RouteModelDto setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
        return this;
    }
}
