package com.sismics.docs.resource;

import android.content.Context;

import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.sismics.docs.listener.JsonHttpResponseHandler;

import java.io.InputStream;
import java.security.KeyStore;


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

    /**
     * DELETE /file/id.
     *
     * @param context Context
     * @param id ID
     * @param responseHandler Callback
     */
    public static void delete(Context context, String id, JsonHttpResponseHandler responseHandler) {
        init(context);

        client.delete(getApiUrl(context) + "/file/" + id, responseHandler);
    }

    /**
     * PUT /file.
     *
     * @param context Context
     * @param documentId Document ID
     * @param is Input stream
     * @param responseHandler Callback
     * @throws Exception
     */
    public static void addSync(Context context, String documentId, InputStream is, JsonHttpResponseHandler responseHandler) throws Exception {
        init(context);

        SyncHttpClient client = new SyncHttpClient();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        MySSLSocketFactory2 sf = new MySSLSocketFactory2(trustStore);
        sf.setHostnameVerifier(MySSLSocketFactory2.ALLOW_ALL_HOSTNAME_VERIFIER);
        client.setSSLSocketFactory(sf);
        client.setCookieStore(new PersistentCookieStore(context));
        client.setUserAgent(USER_AGENT);
        client.addHeader("Accept-Language", ACCEPT_LANGUAGE);

        RequestParams params = new RequestParams();
        params.put("id", documentId);
        params.put("file", is, "file", "application/octet-stream", true);
        client.put(getApiUrl(context) + "/file", params, responseHandler);
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
