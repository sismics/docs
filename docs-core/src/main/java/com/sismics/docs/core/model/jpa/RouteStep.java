package com.sismics.docs.core.model.jpa;

import com.google.common.base.MoreObjects;
import com.sismics.docs.core.constant.RouteStepTransition;
import com.sismics.docs.core.constant.RouteStepType;

import javax.persistence.*;
import java.util.Date;

/**
 * Route step.
 * 
 * @author bgamard
 */
@Entity
@Table(name = "T_ROUTE_STEP")
public class RouteStep {
    /**
     * Route step ID.
     */
    @Id
    @Column(name = "RTP_ID_C", length = 36)
    private String id;

    /**
     * Route ID.
     */
    @Column(name = "RTP_IDROUTE_C", nullable = false, length = 36)
    private String routeId;

    /**
     * Name.
     */
    @Column(name = "RTP_NAME_C", nullable = false, length = 200)
    private String name;

    /**
     * Type.
     */
    @Column(name = "RTP_TYPE_C", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RouteStepType type;

    /**
     * Transition.
     */
    @Column(name = "RTP_TRANSITION_C", length = 50)
    @Enumerated(EnumType.STRING)
    private RouteStepTransition transition;

    /**
     * Comment.
     */
    @Column(name = "RTP_COMMENT_C", length = 500)
    private String comment;

    /**
     * Transitions JSON data.
     */
    @Column(name = "RTP_TRANSITIONS_C", length = 2000)
    private String transitions;

    /**
     * Target ID (user or group).
     */
    @Column(name = "RTP_IDTARGET_C", nullable = false, length = 36)
    private String targetId;

    /**
     * Validator user ID.
     */
    @Column(name = "RTP_IDVALIDATORUSER_C", length = 36)
    private String validatorUserId;

    /**
     * Order.
     */
    @Column(name = "RTP_ORDER_N", nullable = false)
    private Integer order;

    /**
     * Creation date.
     */
    @Column(name = "RTP_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * End date.
     */
    @Column(name = "RTP_ENDDATE_D")
    private Date endDate;

    /**
     * Deletion date.
     */
    @Column(name = "RTP_DELETEDATE_D")
    private Date deleteDate;

    public String getId() {
        return id;
    }

    public RouteStep setId(String id) {
        this.id = id;
        return this;
    }

    public String getRouteId() {
        return routeId;
    }

    public RouteStep setRouteId(String routeId) {
        this.routeId = routeId;
        return this;
    }

    public String getName() {
        return name;
    }

    public RouteStep setName(String name) {
        this.name = name;
        return this;
    }

    public RouteStepType getType() {
        return type;
    }

    public RouteStep setType(RouteStepType type) {
        this.type = type;
        return this;
    }

    public RouteStepTransition getTransition() {
        return transition;
    }

    public RouteStep setTransition(RouteStepTransition transition) {
        this.transition = transition;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public RouteStep setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getTargetId() {
        return targetId;
    }

    public RouteStep setTargetId(String targetId) {
        this.targetId = targetId;
        return this;
    }

    public Integer getOrder() {
        return order;
    }

    public RouteStep setOrder(Integer order) {
        this.order = order;
        return this;
    }

    public String getValidatorUserId() {
        return validatorUserId;
    }

    public RouteStep setValidatorUserId(String validatorUserId) {
        this.validatorUserId = validatorUserId;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public RouteStep setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public Date getEndDate() {
        return endDate;
    }

    public RouteStep setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public Date getDeleteDate() {
        return deleteDate;
    }

    public RouteStep setDeleteDate(Date deleteDate) {
        this.deleteDate = deleteDate;
        return this;
    }

    public String getTransitions() {
        return transitions;
    }

    public RouteStep setTransitions(String transitions) {
        this.transitions = transitions;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("routeId", routeId)
                .add("name", name)
                .add("type", type)
                .add("transition", transition)
                .add("comment", comment)
                .add("targetId", targetId)
                .add("order", order)
                .add("createDate", createDate)
                .add("endDate", endDate)
                .toString();
    }
}
