package com.starkcorp.bkrik;

/**
 * Created by anirudh on 7/22/2018.
 */

import android.content.SearchRecentSuggestionsProvider;

import static android.content.SearchRecentSuggestionsProvider.DATABASE_MODE_QUERIES;

public class RecentSearch extends SearchRecentSuggestionsProvider {

    public static final String AUTHORITY =
            RecentSearch.class.getName();

    public static final int MODE = DATABASE_MODE_QUERIES;

    public RecentSearch() {
        setupSuggestions(AUTHORITY, MODE);
    }
}