package com.sismics.docs.resource;

import android.content.Context;

import com.sismics.docs.listener.JsonHttpResponseHandler;


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
     * @param responseHandler Callback
     */
    public static void list(Context context, String documentId, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.get(getApiUrl(context) + "/comment/" + documentId, responseHandler);
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
