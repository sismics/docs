package com.sismics.docs.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Search query builder.
 *
 * @author bgamard.
 */
public class SearchQueryBuilder {
    /**
     * The query.
     */
    private StringBuilder query;

    /**
     * Search separator.
     */
    private static String SEARCH_SEPARATOR = " ";

    /**
     * Search date format.
     */
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Build a query.
     */
    public SearchQueryBuilder() {
        query = new StringBuilder();
    }

    /**
     * Add a simple search criteria.
     *
     * @param simpleSearch Simple search criteria
     * @return The builder
     */
    public SearchQueryBuilder simpleSearch(String simpleSearch) {
        if (isValid(simpleSearch)) {
            query.append(SEARCH_SEPARATOR)
                    .append("simple:")
                    .append(simpleSearch);
        }
        return this;
    }

    /**
     * Add a fulltext search criteria.
     *
     * @param fulltextSearch Fulltext search criteria
     * @return The builder
     */
    public SearchQueryBuilder fulltextSearch(String fulltextSearch) {
        if (isValid(fulltextSearch)) {
            query.append(SEARCH_SEPARATOR)
                    .append("full:")
                    .append(fulltextSearch);
        }
        return this;
    }

    /**
     * Add a creator criteria.
     *
     * @param creator Creator criteria
     * @return The builder
     */
    public SearchQueryBuilder creator(String creator) {
        if (isValid(creator)) {
            query.append(SEARCH_SEPARATOR)
                    .append("by:")
                    .append(creator);
        }
        return this;
    }

    /**
     * Add a language criteria.
     *
     * @param language Language criteria
     * @return The builder
     */
    public SearchQueryBuilder language(String language) {
        if (isValid(language)) {
            query.append(SEARCH_SEPARATOR)
                    .append("lang:")
                    .append(language);
        }
        return this;
    }

    /**
     * Add a shared criteria.
     *
     * @param shared Shared criteria
     * @return The builder
     */
    public SearchQueryBuilder shared(boolean shared) {
        if (shared) {
            query.append(SEARCH_SEPARATOR).append("shared:yes");
        }
        return this;
    }

    /**
     * Add a tag criteria.
     *
     * @param tag Tag criteria
     * @return The builder
     */
    public SearchQueryBuilder tag(String tag) {
        query.append(SEARCH_SEPARATOR)
                .append("tag:")
                .append(tag);
        return this;
    }

    /**
     * Add a before date criteria.
     *
     * @param before Before date criteria
     * @return The builder
     */
    public SearchQueryBuilder before(Date before) {
        if (before != null) {
            query.append(SEARCH_SEPARATOR)
                    .append("before:")
                    .append(DATE_FORMAT.format(before));
        }
        return this;
    }

    /**
     * Add an after date criteria.
     *
     * @param after After date criteria
     * @return The builder
     */
    public SearchQueryBuilder after(Date after) {
        if (after != null) {
            query.append(SEARCH_SEPARATOR)
                    .append("after:")
                    .append(DATE_FORMAT.format(after));
        }
        return this;
    }

    /**
     * Build the query.
     *
     * @return The query
     */
    public String build() {
        return query.toString();
    }

    /**
     * Return true if the search criteria is valid.
     *
     * @param criteria Search criteria
     * @return True if the search criteria is valid
     */
    private boolean isValid(String criteria) {
        return criteria != null && !criteria.trim().isEmpty();
    }
}
