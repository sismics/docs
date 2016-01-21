package com.sismics.docs.listener;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * An HTTP callback.
 *
 * @author bgamard.
 */
public class HttpCallback {
    public void onSuccess(JSONObject json) {
        // Implement me
    }

    public void onFailure(JSONObject json, Exception e) {
        // Implement me
    }

    public void onFinish() {
        // Implement me
    }

    /**
     * Build an OkHttp Callback from a HttpCallback.
     *
     * @param httpCallback HttpCallback
     * @return OkHttp Callback
     */
    public static Callback buildOkHttpCallback(final HttpCallback httpCallback) {
        return new Callback() {
            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                final String body = response.body().string();

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            try {
                                httpCallback.onSuccess(new JSONObject(body));
                            } catch (Exception e) {
                                httpCallback.onFailure(null, e);
                            }
                        } else {
                            try {
                                httpCallback.onFailure(new JSONObject(body), null);
                            } catch (Exception e) {
                                httpCallback.onFailure(null, e);
                            }
                        }

                        httpCallback.onFinish();
                    }
                });
            }

            @Override
            public void onFailure(final Call call, final IOException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        httpCallback.onFailure(null, e);
                        httpCallback.onFinish();
                    }
                });
            }
        };
    }
}
