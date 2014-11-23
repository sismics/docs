package com.sismics.docs.resource;

import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Access to /tag API.
 * 
 * @author bgamard
 */
public class TagResource extends BaseResource {
    /**
     * GET /tag/stats.
     *
     * @param context Context
     * @param responseHandler Callback
     */
    public static void stats(Context context, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.get(getApiUrl(context) + "/tag/stats", responseHandler);
    }
}
