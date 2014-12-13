package com.sismics.docs.resource;

import android.content.Context;

import com.loopj.android.http.RequestParams;
import com.sismics.docs.listener.JsonHttpResponseHandler;

import java.util.Set;

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
     * @param responseHandler Callback
     */
    public static void list(Context context, int offset, String query, JsonHttpResponseHandler responseHandler) {
        init(context);
        
        RequestParams params = new RequestParams();
        params.put("limit", 20);
        params.put("offset", offset);
        params.put("sort_column", 3);
        params.put("asc", false);
        params.put("search", query);
        client.get(getApiUrl(context) + "/document/list", params, responseHandler);
    }

    /**
     * GET /document/id.
     *
     * @param context Context
     * @param id ID
     * @param responseHandler Callback
     */
    public static void get(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.get(getApiUrl(context) + "/document/" + id, responseHandler);
    }

    /**
     * DELETE /document/id.
     *
     * @param context Context
     * @param id ID
     * @param responseHandler Callback
     */
    public static void delete(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.delete(getApiUrl(context) + "/document/" + id, responseHandler);
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
     * @param responseHandler Callback
     */
    public static void add(Context context, String title, String description,
                           Set<String> tagIdList, String language, long createDate, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        params.put("title", title);
        params.put("description", description);
        params.put("tags", tagIdList);
        params.put("language", language);
        params.put("create_date", createDate);
        client.put(getApiUrl(context) + "/document", params, responseHandler);
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
     * @param responseHandler Callback
     */
    public static void edit(Context context, String id, String title, String description,
                           Set<String> tagIdList, String language, long createDate, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        params.put("title", title);
        params.put("description", description);
        params.put("tags", tagIdList);
        params.put("language", language);
        params.put("create_date", createDate);
        client.post(getApiUrl(context) + "/document/" + id, params, responseHandler);
    }

    /**
     * Cancel pending requests.
     *
     * @param context Context
     */
    public static void cancel(Context context) {
        client.cancelRequests(context, true);
    }
}
