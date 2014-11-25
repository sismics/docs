package com.sismics.docs.resource;

import android.content.Context;

import com.sismics.docs.listener.JsonHttpResponseHandler;


/**
 * Access to /file API.
 * 
 * @author bgamard
 */
public class FileResource extends BaseResource {
    /**
     * GET /file/list.
     *
     * @param context Context
     * @param documentId Document ID
     * @param responseHandler Callback
     */
    public static void list(Context context, String documentId, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.get(getApiUrl(context) + "/file/list?id=" + documentId, responseHandler);
    }
}
