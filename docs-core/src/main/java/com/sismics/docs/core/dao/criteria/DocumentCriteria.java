package com.sismics.docs.core.dao.criteria;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Document criteria.
 *
 * @author bgamard 
 */
public class DocumentCriteria {
    /**
     * ACL target ID list.
     */
    private List<String> targetIdList;
    
    /**
     * Search query.
     */
    private String simpleSearch;
    
    /**
     * Full content search query.
     */
    private String fullSearch;
    
    /**
     * Minimum creation date.
     */
    private Date createDateMin;
    
    /**
     * Maximum creation date.
     */
    private Date createDateMax;
    
    /**
     * Minimum update date.
     */
    private Date updateDateMin;

    /**
     * Maximum update date.
     */
    private Date updateDateMax;

    /**
     * Tag IDs.
     * The first level list will be AND'ed and the second level list will be OR'ed.
     */
    private List<List<String>> tagIdList = new ArrayList<>();
    
    /**
     * Tag IDs to exclude.
     * The first and second level list will be excluded.
     */
    private List<List<String>> excludedTagIdList = new ArrayList<>();

    /**
     * Shared status.
     */
    private Boolean shared;
    
    /**
     * Language.
     */
    private String language;
    
    /**
     * Creator ID.
     */
    private String creatorId;

    /**
     * A route is active.
     */
    private Boolean activeRoute;

    /**
     * MIME type of a file.
     */
    private String mimeType;

    /**
     * Titles to include.
     */
    private List<String> titleList = new ArrayList<>();

    public List<String> getTargetIdList() {
        return targetIdList;
    }

    public void setTargetIdList(List<String> targetIdList) {
        this.targetIdList = targetIdList;
    }

    public String getSimpleSearch() {
        return simpleSearch;
    }

    public void setSimpleSearch(String search) {
        this.simpleSearch = search;
    }

    public String getFullSearch() {
        return fullSearch;
    }

    public void setFullSearch(String fullSearch) {
        this.fullSearch = fullSearch;
    }

    public Date getCreateDateMin() {
        return createDateMin;
    }

    public void setCreateDateMin(Date createDateMin) {
        this.createDateMin = createDateMin;
    }

    public Date getCreateDateMax() {
        return createDateMax;
    }

    public void setCreateDateMax(Date createDateMax) {
        this.createDateMax = createDateMax;
    }

    public List<List<String>> getTagIdList() {
        return tagIdList;
    }

    public List<List<String>> getExcludedTagIdList() {
        return excludedTagIdList;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
 
    public Date getUpdateDateMin() {
        return updateDateMin;
    }

    public void setUpdateDateMin(Date updateDateMin) {
        this.updateDateMin = updateDateMin;
    }

    public Date getUpdateDateMax() {
        return updateDateMax;
    }

    public void setUpdateDateMax(Date updateDateMax) {
        this.updateDateMax = updateDateMax;
    }

    public Boolean getActiveRoute() {
        return activeRoute;
    }

    public void setActiveRoute(Boolean activeRoute) {
        this.activeRoute = activeRoute;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public List<String> getTitleList() {
        return titleList;
    }
}
