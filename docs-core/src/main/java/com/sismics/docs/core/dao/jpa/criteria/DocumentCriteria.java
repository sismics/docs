package com.sismics.docs.core.dao.jpa.criteria;

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
    private String search;
    
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
     * Tag IDs.
     */
    private List<String> tagIdList;
    
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
    
    public List<String> getTargetIdList() {
        return targetIdList;
    }

    public void setTargetIdList(List<String> targetIdList) {
        this.targetIdList = targetIdList;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
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

    public List<String> getTagIdList() {
        return tagIdList;
    }

    public void setTagIdList(List<String> tagIdList) {
        this.tagIdList = tagIdList;
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
}
