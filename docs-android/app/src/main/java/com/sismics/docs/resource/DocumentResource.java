package com.sismics.docs.resource;

import android.content.Context;

import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.util.OkHttpUtil;

import java.util.Set;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Access to /document API.
 * 
 * @author bgamard
 */
public class DocumentResource extends BaseResource {
    /**
     * GET /document/list.
     *
     * @param context Context
     * @param offset Offset
     * @param query Search query
     * @param callback Callback
     */
    public static void list(Context context, int offset, String query, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/document/list")
                        .newBuilder()
                        .addQueryParameter("limit", "20")
                        .addQueryParameter("offset", Integer.toString(offset))
                        .addQueryParameter("sort_column", "3")
                        .addQueryParameter("asc", "false")
                        .addQueryParameter("search", query)
                        .build())
                .get()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * GET /document/id.
     *
     * @param context Context
     * @param id ID
     * @param callback Callback
     */
    public static void get(Context context, String id, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/document/" + id))
                .get()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * DELETE /document/id.
     *
     * @param context Context
     * @param id ID
     * @param callback Callback
     */
    public static void delete(Context context, String id, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/document/" + id))
                .delete()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * PUT /document.
     *
     * @param context Context
     * @param title Title
     * @param description Description
     * @param tagIdList Tags ID list
     * @param language Language
     * @param createDate Create date
     * @param callback Callback
     */
    public static void add(Context context, String title, String description,
                           Set<String> tagIdList, String language, long createDate, HttpCallback callback) {
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("title", title)
                .add("description", description)
                .add("language", language)
                .add("create_date", Long.toString(createDate));
        for( String tagId : tagIdList) {
            formBuilder.add("tags", tagId);
        }

        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/document"))
                .put(formBuilder.build())
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * POST /document/id.
     *
     * @param context Context
     * @param id ID
     * @param title Title
     * @param description Description
     * @param tagIdList Tags ID list
     * @param language Language
     * @param createDate Create date
     * @param callback Callback
     */
    public static void edit(Context context, String id, String title, String description,
                           Set<String> tagIdList, String language, long createDate, HttpCallback callback) {
        FormBody.Builder formBuilder = new FormBody.Builder()
                .add("title", title)
                .add("description", description)
                .add("language", language)
                .add("create_date", Long.toString(createDate));
        for( String tagId : tagIdList) {
            formBuilder.add("tags", tagId);
        }

        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/document/" + id))
                .post(formBuilder.build())
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }
}
