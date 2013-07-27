package com.sismics.docs.core.util.jpa;

import java.util.Map;

/**
 * Query parameters.
 *
 * @author jtremeaux 
 */
public class QueryParam {

    /**
     * Query string.
     */
    private String queryString;
    
    /**
     * Query parameters.
     */
    private Map<String, Object> parameterMap;

    /**
     * Constructor of QueryParam.
     * 
     * @param queryString Query string
     * @param parameterMap Query parameters
     */
    public QueryParam(String queryString, Map<String, Object> parameterMap) {
        this.queryString = queryString;
        this.parameterMap = parameterMap;
    }

    /**
     * Getter of queryString.
     *
     * @return queryString
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Getter of parameterMap.
     *
     * @return parameterMap
     */
    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }
    
}
