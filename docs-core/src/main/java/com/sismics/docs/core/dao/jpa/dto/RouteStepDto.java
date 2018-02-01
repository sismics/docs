package com.sismics.docs.core.dao.jpa.dto;

import com.sismics.docs.core.constant.RouteStepTransition;
import com.sismics.docs.core.constant.RouteStepType;

/**
 * Route step DTO.
 *
 * @author bgamard 
 */
public class RouteStepDto {
    /**
     * Route step ID.
     */
    private String id;

    /**
     * Name.
     */
    private String name;

    /**
     * Type.
     */
    private RouteStepType type;

    /**
     * Transition.
     */
    private RouteStepTransition transition;

    /**
     * Comment.
     */
    private String comment;

    /**
     * Target ID (user or group).
     */
    private String targetId;

    /**
     * End date.
     */
    private Long endDateTimestamp;

    public String getId() {
        return id;
    }

    public RouteStepDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public RouteStepDto setName(String name) {
        this.name = name;
        return this;
    }

    public RouteStepType getType() {
        return type;
    }

    public RouteStepDto setType(RouteStepType type) {
        this.type = type;
        return this;
    }

    public RouteStepTransition getTransition() {
        return transition;
    }

    public RouteStepDto setTransition(RouteStepTransition transition) {
        this.transition = transition;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public RouteStepDto setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getTargetId() {
        return targetId;
    }

    public RouteStepDto setTargetId(String targetId) {
        this.targetId = targetId;
        return this;
    }

    public Long getEndDateTimestamp() {
        return endDateTimestamp;
    }

    public RouteStepDto setEndDateTimestamp(Long endDateTimestamp) {
        this.endDateTimestamp = endDateTimestamp;
        return this;
    }
}
