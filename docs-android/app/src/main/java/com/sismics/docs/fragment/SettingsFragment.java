package com.sismics.docs.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.widget.Toast;

import com.sismics.docs.R;
import com.sismics.docs.provider.RecentSuggestionsProvider;
import com.sismics.docs.util.ApplicationUtil;
import com.sismics.docs.util.OkHttpUtil;
import com.sismics.docs.util.PreferenceUtil;

/**
 * Settings fragment.
 *
 * @author bgamard.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Initialize summaries
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, PreferenceUtil.PREF_CACHE_SIZE);


        // Handle clearing the recent search history
        Preference clearHistoryPref = findPreference("pref_clearHistory");
        clearHistoryPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        RecentSuggestionsProvider.AUTHORITY, RecentSuggestionsProvider.MODE);
                suggestions.clearHistory();
                Toast.makeText(getActivity(), R.string.pref_clear_history_success, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        // Handle clearing the cache
        Preference clearCachePref = findPreference("pref_clearCache");
        clearCachePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                OkHttpUtil.clearCache(getActivity());
                Toast.makeText(getActivity(), R.string.pref_clear_cache_success, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        // Initialize static text preferences
        Preference versionPref = findPreference("pref_version");
        versionPref.setSummary(getString(R.string.version) + " " + ApplicationUtil.getVersionName(getActivity())
                + " | " + getString(R.string.build) + " " + ApplicationUtil.getVersionCode(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }
}
