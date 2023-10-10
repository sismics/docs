package com.sismics.docs.core.util.jpa;

import jakarta.persistence.Query;
import java.util.List;

/**
 * Utilities for paginated lists.
 * 
 * @author jtremeaux
 */
public class PaginatedLists {
    /**
     * Default size of a page.
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Maximum size of a page.
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Constructs a paginated list.
     * 
     * @param pageSize Size of the page
     * @param offset Offset of the page
     * @return Paginated list
     */
    public static <E> PaginatedList<E> create(Integer pageSize, Integer offset) {
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (offset == null) {
            offset = 0;
        }
        if (pageSize > MAX_PAGE_SIZE) {
            pageSize = MAX_PAGE_SIZE;
        }
        return new PaginatedList<>(pageSize, offset);
    }
    
    /**
     * Constructs a paginated list with default parameters.
     * 
     * @return Paginated list
     */
    public static <E> PaginatedList<E> create() {
        return create(null, null);
    }
    
    /**
     * Executes a native count(*) request to count the number of results.
     * 
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam Query parameters
     */
    private static <E> void executeCountQuery(PaginatedList<E> paginatedList, QueryParam queryParam) {
        StringBuilder sb = new StringBuilder("select count(*) as result_count from (");
        sb.append(queryParam.getQueryString());
        sb.append(") as t1");

        QueryParam countQueryParam = new QueryParam(sb.toString(), queryParam.getParameterMap());
        
        Query q = QueryUtil.getNativeQuery(countQueryParam);
        
        Number resultCount = (Number) q.getSingleResult();
        paginatedList.setResultCount(resultCount.intValue());
    }

    /**
     * Executes a query and returns the data of the current page.
     * 
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam Query parameters
     * @return List of results
     */
    @SuppressWarnings("unchecked")
    private static <E> List<Object[]> executeResultQuery(PaginatedList<E> paginatedList, QueryParam queryParam) {
        Query q = QueryUtil.getNativeQuery(queryParam);
        
        q.setFirstResult(paginatedList.getOffset());
        q.setMaxResults(paginatedList.getLimit());
        return q.getResultList();
    }

    /**
     * Executes a paginated request with 2 native queries (one to count the number of results, and one to return the page).
     * 
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam Query parameters
     * @param sortCriteria Sort criteria
     * @return List of results
     */
    public static <E> List<Object[]> executePaginatedQuery(PaginatedList<E> paginatedList, QueryParam queryParam, SortCriteria sortCriteria) {
        QueryParam sortedQueryParam = QueryUtil.getSortedQueryParam(queryParam, sortCriteria);
        executeCountQuery(paginatedList, sortedQueryParam);
        return executeResultQuery(paginatedList, sortedQueryParam);
    }
}
