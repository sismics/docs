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
     * Target ID (user or group).
     */
    @Column(name = "RTP_IDTARGET_C", nullable = false, length = 36)
    private String targetId;

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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("routeId", routeId)
                .add("name", name)
                .add("type", type)
                .add("transition", transition)
                .add("targetId", targetId)
                .add("order", order)
                .add("createDate", createDate)
                .add("endDate", endDate)
                .toString();
    }
}
