package com.sismics.docs.resource;

import android.content.Context;

import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.util.OkHttpUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;


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
     * @param callback Callback
     */
    public static void list(Context context, String documentId, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/file/list")
                        .newBuilder()
                        .addQueryParameter("id", documentId)
                        .build())
                .get()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * DELETE /file/id.
     *
     * @param context Context
     * @param id ID
     * @param callback Callback
     */
    public static void delete(Context context, String id, HttpCallback callback) {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/file/" + id))
                .delete()
                .build();
        OkHttpUtil.buildClient(context)
                .newCall(request)
                .enqueue(HttpCallback.buildOkHttpCallback(callback));
    }

    /**
     * PUT /file.
     *
     * @param context Context
     * @param documentId Document ID
     * @param is Input stream
     * @param callback Callback
     * @throws Exception
     */
    public static void addSync(Context context, String documentId, final InputStream is, HttpCallback callback) throws Exception {
        Request request = new Request.Builder()
                .url(HttpUrl.parse(getApiUrl(context) + "/file"))
                .put(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("id", documentId)
                        .addFormDataPart("file", "file", new RequestBody() {
                            @Override
                            public MediaType contentType() {
                                return MediaType.parse("application/octet-stream");
                            }

                            @Override
                            public void writeTo(BufferedSink sink) throws IOException {
                                Source source = Okio.source(is);
                                try {
                                    sink.writeAll(source);
                                } finally {
                                    Util.closeQuietly(source);
                                }
                            }
                        })
                        .build())
                .build();
        Response response = OkHttpUtil.buildClient(context)
                .newCall(request)
                .execute();

        // Call the right callback
        final String body = response.body().string();
        if (response.isSuccessful()) {
            try {
                callback.onSuccess(new JSONObject(body));
            } catch (Exception e) {
                callback.onFailure(null, e);
            }
        } else {
            try {
                callback.onFailure(new JSONObject(body), null);
            } catch (Exception e) {
                callback.onFailure(null, e);
            }
        }

        callback.onFinish();
    }
}
