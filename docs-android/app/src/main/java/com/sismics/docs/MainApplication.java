package com.sismics.docs;

import android.app.Application;

import com.androidquery.callback.BitmapAjaxCallback;
import com.sismics.docs.model.application.ApplicationContext;
import com.sismics.docs.util.PreferenceUtil;

import org.json.JSONObject;

/**
 * Main application.
 * 
 * @author bgamard
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        // Fetching /user/info from cache
        JSONObject json = PreferenceUtil.getCachedJson(getApplicationContext(), PreferenceUtil.PREF_CACHED_USER_INFO_JSON);
        ApplicationContext.getInstance().setUserInfo(getApplicationContext(), json);

        // TODO Fullscreen preview
        // TODO Downloading
        // TODO Sharing
        // TODO Shared status
        // TODO Tags on document
        // TODO Error feedback
        // TODO Infinite scrolling on documents
        // TODO Searching
        // TODO Printing

        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        BitmapAjaxCallback.clearCache();
    }
}
