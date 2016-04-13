package com.sismics.docs.resource;

import android.content.Context;

import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.util.OkHttpUtil;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;

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
     * @param callback Callback
     */
    public static void login(Context context, String username, String password, String code, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/user/login"))
                .post(new FormBody.Builder()
                        .add("username", username)
                        .add("password", password)
                        .add("code", code)
                        .add("remember", "true")
                        .build())
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * GET /user.
     *
     * @param context Context
     * @param callback Callback
     */
    public static void info(Context context, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/user"))
                .get()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * GET /user/username.
     *
     * @param context Context
     * param username Username
     * @param callback Callback
     */
    public static void get(Context context, String username, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/user/" + username))
                .get()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }
    
    /**
     * POST /user/logout.
     *
     * @param context Context
     * @param callback Callback
     */
    public static void logout(Context context, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/user/logout"))
                .post(new FormBody.Builder().build())
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }
}
