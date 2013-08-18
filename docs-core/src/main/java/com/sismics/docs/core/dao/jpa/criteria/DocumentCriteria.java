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
     * User ID.
     */
    private String userId;
    
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
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of search.
     *
     * @return the search
     */
    public String getSearch() {
        return search;
    }

    /**
     * Setter of search.
     *
     * @param search search
     */
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Getter of fullSearch.
     *
     * @return the fullSearch
     */
    public String getFullSearch() {
        return fullSearch;
    }

    /**
     * Setter of fullSearch.
     *
     * @param fullSearch fullSearch
     */
    public void setFullSearch(String fullSearch) {
        this.fullSearch = fullSearch;
    }

    /**
     * Getter of createDateMin.
     *
     * @return the createDateMin
     */
    public Date getCreateDateMin() {
        return createDateMin;
    }

    /**
     * Setter of createDateMin.
     *
     * @param createDateMin createDateMin
     */
    public void setCreateDateMin(Date createDateMin) {
        this.createDateMin = createDateMin;
    }

    /**
     * Getter of createDateMax.
     *
     * @return the createDateMax
     */
    public Date getCreateDateMax() {
        return createDateMax;
    }

    /**
     * Setter of createDateMax.
     *
     * @param createDateMax createDateMax
     */
    public void setCreateDateMax(Date createDateMax) {
        this.createDateMax = createDateMax;
    }

    /**
     * Getter of tagIdList.
     *
     * @return the tagIdList
     */
    public List<String> getTagIdList() {
        return tagIdList;
    }

    /**
     * Setter of tagIdList.
     *
     * @param tagIdList tagIdList
     */
    public void setTagIdList(List<String> tagIdList) {
        this.tagIdList = tagIdList;
    }

    /**
     * Getter of shared.
     *
     * @return the shared
     */
    public Boolean getShared() {
        return shared;
    }

    /**
     * Setter of shared.
     *
     * @param shared shared
     */
    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    /**
     * Getter of language.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Setter of language.
     *
     * @param language language
     */
    public void setLanguage(String language) {
        this.language = language;
    }
}
