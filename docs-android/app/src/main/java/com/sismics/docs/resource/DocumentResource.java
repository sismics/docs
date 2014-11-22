package com.sismics.docs.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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
}
