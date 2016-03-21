package com.sismics.docs.resource;

import android.content.Context;

import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.util.OkHttpUtil;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Access to /auditlog API.
 * 
 * @author bgamard
 */
public class AuditLogResource extends BaseResource {
    /**
     * GET /auditlog.
     *
     * @param context Context
     * @param documentId Document ID
     * @param callback Callback
     */
    public static void list(Context context, String documentId, HttpCallback callback) {
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(getApiUrl(context) + "/auditlog")
                .newBuilder();
        if (documentId != null) {
            httpUrlBuilder.addQueryParameter("document", documentId);
        }
        Request request = new Request.Builder()
                .url(httpUrlBuilder.build())
                .get()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }
}
