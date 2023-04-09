package com.sismics.docs.core.util.jpa;

import java.util.Map.Entry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import com.sismics.util.context.ThreadLocalContext;

/**
 * Query utilities.
 *
 * @author jtremeaux 
 */
public class QueryUtil {

    /**
     * Creates a native query from the query parameters.
     * 
     * @param queryParam Query parameters
     * @return Native query
     */
    public static Query getNativeQuery(QueryParam queryParam) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query query = em.createNativeQuery(queryParam.getQueryString());
        for (Entry<String, Object> entry : queryParam.getParameterMap().entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        return query;
    }
    
    /**
     * Returns sorted query parameters.
     * 
     * @param queryParam Query parameters
     * @param sortCriteria Sort criteria
     * @return Sorted query parameters
     */
    public static QueryParam getSortedQueryParam(QueryParam queryParam, SortCriteria sortCriteria) {
        StringBuilder sb = new StringBuilder(queryParam.getQueryString());
        if (sortCriteria != null) {
            sb.append(" order by c");
            sb.append(sortCriteria.getColumn());
            sb.append(sortCriteria.isAsc() ? " asc" : " desc");
        }
        
        return new QueryParam(sb.toString(), queryParam.getParameterMap());
    }
}
