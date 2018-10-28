package com.sismics.docs.core.dao.criteria;


import java.util.List;

/**
 * Route model criteria.
 *
 * @author bgamard
 */
public class RouteModelCriteria {
    /**
     * ACL target ID list.
     */
    private List<String> targetIdList;

    public List<String> getTargetIdList() {
        return targetIdList;
    }

    public RouteModelCriteria setTargetIdList(List<String> targetIdList) {
        this.targetIdList = targetIdList;
        return this;
    }
}
