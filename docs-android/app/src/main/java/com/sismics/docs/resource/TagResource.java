package com.sismics.docs.resource;

import android.content.Context;

import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.util.OkHttpUtil;

import okhttp3.HttpUrl;
import okhttp3.Request;


/**
 * Access to /tag API.
 * 
 * @author bgamard
 */
public class TagResource extends BaseResource {
    /**
     * GET /tag/list.
     *
     * @param context Context
     * @param callback Callback
     */
    public static void list(Context context, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/tag/list"))
                .get()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }
}
