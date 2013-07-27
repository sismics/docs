package com.sismics.docs.core.util.jpa;

import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

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
}
