package com.sismics.docs.resource;

import android.content.Context;

import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.util.OkHttpUtil;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;


/**
 * Access to /comment API.
 * 
 * @author bgamard
 */
public class CommentResource extends BaseResource {
    /**
     * GET /comment/id.
     *
     * @param context Context
     * @param documentId Document ID
     * @param callback Callback
     */
    public static void list(Context context, String documentId, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/comment/" + documentId))
                .get()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * PUT /comment.
     *
     * @param context Context
     * @param documentId Document ID
     * @param content Comment content
     * @param callback Callback
     */
    public static void add(Context context, String documentId, String content, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/comment"))
                .put(new FormBody.Builder()
                        .add("id", documentId)
                        .add("content", content)
                        .build())
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * DELETE /comment/id.
     *
     * @param context Context
     * @param commentId Comment ID
     * @param callback Callback
     */
    public static void remove(Context context, String commentId, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/comment/" + commentId))
                .delete()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }
}
