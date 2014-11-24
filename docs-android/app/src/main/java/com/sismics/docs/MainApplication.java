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
        // Fetching GET /user from cache
        JSONObject json = PreferenceUtil.getCachedJson(getApplicationContext(), PreferenceUtil.PREF_CACHED_USER_INFO_JSON);
        ApplicationContext.getInstance().setUserInfo(getApplicationContext(), json);

        // TODO Edit sharing
        // TODO Tags loading feedback
        // TODO Redraw flags
        // TODO Error feedback (all REST request, even login)
        // TODO Fullscreen preview
        // TODO Caching preferences

        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        BitmapAjaxCallback.clearCache();
    }
}
