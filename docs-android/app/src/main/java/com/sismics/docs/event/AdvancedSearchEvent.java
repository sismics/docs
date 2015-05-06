package com.sismics.docs.event;

/**
 * Advanced search event.
 *
 * @author bgamard.
 */
public class AdvancedSearchEvent {
    /**
     * Search query.
     */
    private String query;

    /**
     * Create an advanced search event.
     *
     * @param query Query
     */
    public AdvancedSearchEvent(String query) {
        this.query = query;
    }

    /**
     * Getter of query.
     *
     * @return query
     */
    public String getQuery() {
        return query;
    }
}
