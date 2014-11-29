package com.sismics.docs.resource;

import android.content.Context;

import com.loopj.android.http.RequestParams;
import com.sismics.docs.listener.JsonHttpResponseHandler;


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
     * @param responseHandler Callback
     */
    public static void add(Context context, String documentId, String name, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        params.put("id", documentId);
        params.put("name", name);
        client.put(getApiUrl(context) + "/share", params, responseHandler);
    }

    /**
     * DELETE /share.
     *
     * @param context Context
     * @param id ID
     * @param responseHandler Callback
     */
    public static void delete(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.delete(getApiUrl(context) + "/share/" + id, responseHandler);
    }
}
