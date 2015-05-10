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

        // TODO google docs app: right drawer with all actions, with acls, with deep metadatas
        // TODO Provide documents to intent action get content

        super.onCreate();
    }

    @Override
    public void onLowMemory() {
        BitmapAjaxCallback.clearCache();
    }
}
