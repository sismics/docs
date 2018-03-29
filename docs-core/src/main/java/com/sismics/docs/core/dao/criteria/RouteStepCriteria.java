package com.sismics.docs.core.dao.criteria;


/**
 * Route step criteria.
 *
 * @author bgamard
 */
public class RouteStepCriteria {
    /**
     * Document ID.
     */
    private String documentId;

    /**
     * Route ID.
     */
    private String routeId;

    /**
     * End date is null.
     */
    private Boolean endDateIsNull;

    public String getDocumentId() {
        return documentId;
    }

    public RouteStepCriteria setDocumentId(String documentId) {
        this.documentId = documentId;
        return this;
    }

    public String getRouteId() {
        return routeId;
    }

    public RouteStepCriteria setRouteId(String routeId) {
        this.routeId = routeId;
        return this;
    }

    public Boolean getEndDateIsNull() {
        return endDateIsNull;
    }

    public RouteStepCriteria setEndDateIsNull(Boolean endDateIsNull) {
        this.endDateIsNull = endDateIsNull;
        return this;
    }
}
