package com.sismics.docs.core.dao.dto;

/**
 * Route DTO.
 * 
 * @author bgamard
 */
public class RouteDto {
    /**
     * Route ID.
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

    public RouteDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public RouteDto setName(String name) {
        this.name = name;
        return this;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public RouteDto setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
        return this;
    }
}
