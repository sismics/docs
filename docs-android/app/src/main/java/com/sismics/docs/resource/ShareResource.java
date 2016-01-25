package com.sismics.docs.resource;

import android.content.Context;

import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.util.OkHttpUtil;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;


/**
 * Access to /tag API.
 * 
 * @author bgamard
 */
public class ShareResource extends BaseResource {
    /**
     * PUT /share.
     *
     * @param context Context
     * @param documentId Document ID
     * @param name Name
     * @param callback Callback
     */
    public static void add(Context context, String documentId, String name, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/share"))
                .put(new FormBody.Builder()
                        .add("id", documentId)
                        .add("name", name)
                        .build())
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * DELETE /share.
     *
     * @param context Context
     * @param id ID
     * @param callback Callback
     */
    public static void delete(Context context, String id, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/share/" + id))
                .delete()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }
}
