package com.sismics.docs.resource;

import android.content.Context;

import com.loopj.android.http.RequestParams;
import com.sismics.docs.listener.JsonHttpResponseHandler;

/**
 * Access to /user API.
 * 
 * @author bgamard
 */
public class UserResource extends BaseResource {

    /**
     * POST /user/login.
     *
     * @param context Context
     * @param username Username
     * @param password Password
     * @param responseHandler Callback
     */
    public static void login(Context context, String username, String password, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);
        params.put("remember", "true");
        client.post(getApiUrl(context) + "/user/login", params, responseHandler);
    }

    /**
     * GET /user.
     *
     * @param context Context
     * @param responseHandler Callback
     */
    public static void info(Context context, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.get(getApiUrl(context) + "/user", params, responseHandler);
    }
    
    /**
     * POST /user/logout.
     *
     * @param context Context
     * @param responseHandler Callback
     */
    public static void logout(Context context, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        client.post(getApiUrl(context) + "/user/logout", params, responseHandler);
    }
}
