package com.sismics.docs.model.application;

import android.app.Activity;
import android.content.Context;

import com.sismics.docs.listener.CallbackListener;
import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.resource.UserResource;
import com.sismics.docs.util.PreferenceUtil;

import org.json.JSONObject;

/**
 * Global context of the application.
 * 
 * @author bgamard
 */
public class ApplicationContext {
    /**
     * Singleton's instance.
     */
    private static ApplicationContext applicationContext;
    
    /**
     * Response of /user/info
     */
    private JSONObject userInfo;
    
    /**
     * Private constructor.
     */
    private ApplicationContext() {
    }
    
    /**
     * Returns a singleton of ApplicationContext.
     * 
     * @return Singleton of ApplicationContext
     */
    public static ApplicationContext getInstance() {
        if (applicationContext == null) {
            applicationContext = new ApplicationContext();
        }
        return applicationContext;
    }
    
    /**
     * Returns true if current user is logged in.
     *
     * @return True if the current user is logged in
     */
    public boolean isLoggedIn() {
        return userInfo != null && !userInfo.optBoolean("anonymous");
    }

    /**
     * Getter of userInfo
     *
     * @return userInfo
     */
    public JSONObject getUserInfo() {
        return userInfo;
    }

    /**
     * Setter of userInfo
     *
     * @param json userInfo
     */
    public void setUserInfo(Context context, JSONObject json) {
        this.userInfo = json;
        PreferenceUtil.setCachedJson(context, PreferenceUtil.PREF_CACHED_USER_INFO_JSON, json);
    }
    
    /**
     * Asynchronously get user info.
     *
     * @param activity Activity
     * @param callbackListener CallbackListener
     */
    public void fetchUserInfo(final Activity activity, final CallbackListener callbackListener) {
        UserResource.info(activity.getApplicationContext(), new HttpCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                // Save data in application context
                if (!json.optBoolean("anonymous", true)) {
                    setUserInfo(activity.getApplicationContext(), json);
                }
            }

            @Override
            public void onFinish() {
                if (callbackListener != null) {
                    callbackListener.onComplete();
                }
            }
        });
    }
}
