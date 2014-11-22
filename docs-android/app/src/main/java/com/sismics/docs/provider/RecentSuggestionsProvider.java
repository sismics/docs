package com.sismics.docs.provider;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Search recent suggestions provider.
 *
 * @author bgamard.
 */
public class RecentSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.sismics.docs.provider.RecentSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public RecentSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
