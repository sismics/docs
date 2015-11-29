package com.sismics.docs.resource;

import android.content.Context;

import com.loopj.android.http.RequestParams;
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
     * PUT /comment.
     *
     * @param context Context
     * @param documentId Document ID
     * @param content Comment content
     * @param responseHandler Callback
     */
    public static void add(Context context, String documentId, String content, JsonHttpResponseHandler responseHandler) {
        init(context);

        RequestParams params = new RequestParams();
        params.put("id", documentId);
        params.put("content", content);
        client.put(getApiUrl(context) + "/comment", params, responseHandler);
    }

    /**
     * DELETE /comment/id.
     *
     * @param context Context
     * @param commentId Comment ID
     * @param responseHandler Callback
     */
    public static void remove(Context context, String commentId, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.delete(getApiUrl(context) + "/comment/" + commentId, responseHandler);
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
