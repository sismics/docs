package com.sismics.docs.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.sismics.docs.resource.cookie.PersistentCookieStore;

import org.json.JSONObject;

import java.net.HttpCookie;
import java.util.List;

/**
 * Utility class on preferences.
 * 
 * @author bgamard
 */
public class PreferenceUtil {

    public static final String PREF_CACHED_USER_INFO_JSON = "pref_cachedUserInfoJson";
    public static final String PREF_CACHED_TAGS_JSON = "pref_cachedTagsJson";
    public static final String PREF_SERVER_URL = "pref_ServerUrl";
    public static final String PREF_CACHE_SIZE = "pref_cacheSize";

    /**
     * Returns a preference of boolean type.
     *
     * @param context Context
     * @param key Shared preference key
     * @return Shared preference value
     */
    public static boolean getBooleanPreference(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defaultValue);
    }
    
    /**
     * Returns a preference of string type.
     *
     * @param context Context
     * @param key Shared preference key
     * @return Shared preference value
     */
    public static String getStringPreference(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, null);
    }
    
    /**
     * Returns a preference of integer type.
     *
     * @param context Context
     * @param key Shared preference key
     * @return Shared preference value
     */
    public static int getIntegerPreference(Context context, String key, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String pref = sharedPreferences.getString(key, "");
            try {
                return Integer.parseInt(pref);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } catch (ClassCastException e) {
            return sharedPreferences.getInt(key, defaultValue);
        }
        
    }
    
    /**
     * Update JSON cache.
     *
     * @param context Context
     * @param key Shared preference key
     * @param json JSON data
     */
    public static void setCachedJson(Context context, String key, JSONObject json) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(key, json != null ? json.toString() : null).apply();
    }
    
    /**
     * Returns a JSON cache.
     *
     * @param context Context
     * @param key Shared preference key
     * @return JSON data
     */
    public static JSONObject getCachedJson(Context context, String key) {
        try {
            return new JSONObject(getStringPreference(context, key));
        } catch (Exception e) {
            // The cache is not parsable, clean this up
            setCachedJson(context, key, null);
            return null;
        }
    }
    
    /**
     * Update server URL.
     *
     * @param context Context
     */
    public static void setServerUrl(Context context, String serverUrl) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(PREF_SERVER_URL, serverUrl).apply();
    }
    
    /**
     * Empty user caches.
     *
     * @param context Context
     */
    public static void resetUserCache(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = sharedPreferences.edit();
        editor
                .putString(PREF_CACHED_USER_INFO_JSON, null)
                .putString(PREF_CACHED_TAGS_JSON, null)
                .apply();
    }
    
    /**
     * Returns auth token cookie from shared preferences.
     *
     * @param context Context
     * @return Auth token
     */
    public static String getAuthToken(Context context) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        List<HttpCookie> cookieList = cookieStore.getCookies();
        for (HttpCookie cookie : cookieList) {
            if (cookie.getName().equals("auth_token")) {
                return cookie.getValue();
            }
        }
        
        return null;
    }

    /**
     * Clear all auth tokens.
     *
     * @param context Context
     */
    public static void clearAuthToken(Context context) {
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        cookieStore.removeAll();
    }

    /**
     * Returns cleaned server URL.
     *
     * @param context Context
     * @return Server URL
     */
    public static String getServerUrl(Context context) {
        String serverUrl = getStringPreference(context, PREF_SERVER_URL);
        if (serverUrl == null) {
            return null;
        }
        
        // Trim
        serverUrl = serverUrl.trim();
        
        if (!serverUrl.startsWith("http")) {
            // Try to add http
            serverUrl = "http://" + serverUrl;
        }
        
        if (serverUrl.endsWith("/")) {
            // Delete last /
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        
        // Remove /api
        if (serverUrl.endsWith("/api")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 4);
        }
        
        return serverUrl;
    }
}
