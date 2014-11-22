package com.sismics.docs.event;

/**
 * Search event.
 *
 * @author bgamard.
 */
public class SearchEvent {
    /**
     * Search query.
     */
    private String query;

    /**
     * Create a search event.
     *
     * @param query Query
     */
    public SearchEvent(String query) {
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
