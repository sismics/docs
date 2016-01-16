package com.sismics.docs.util;

import android.content.Context;
import android.util.Log;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Utilities for OkHttp.
 *
 * @author bgamard.
 */
public class OkHttpUtil {
    /**
     * Build a Picasso object with base config.
     *
     * @param context Context
     * @param authToken Auth token
     * @return Picasso object
     */
    public static Picasso picasso(Context context, final String authToken) {
        OkHttpClient okHttpClient = buildOkHttpClient()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        final Request original = chain.request();
                        final Request.Builder requestBuilder = original.newBuilder()
                                .header("Cookie", "auth_token=" + authToken)
                                .header("Cache-Control", "max-age=" + (3600 * 24 * 365))
                                .method(original.method(), original.body());
                        return chain.proceed(requestBuilder.build());
                    }
                })
                .cache(new Cache(context.getCacheDir(),
                        PreferenceUtil.getIntegerPreference(context, PreferenceUtil.PREF_CACHE_SIZE, 0) * 1000000))
                .build();

        Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
        picasso.setIndicatorsEnabled(false);
        return picasso;
    }

    /**
     * Clear the cache.
     *
     * @param context Context
     */
    public static void clearCache(Context context) {
        Cache cache = new Cache(context.getCacheDir(),
                PreferenceUtil.getIntegerPreference(context, PreferenceUtil.PREF_CACHE_SIZE, Integer.MAX_VALUE));
        try {
            cache.evictAll();
        } catch (IOException e) {
            Log.e("OKHttpUtil", "Error clearing cache", e);
        }
    }

    /**
     * Build a OkHttpClient accepting all SSL certificates.
     *
     * @return OkHttpClient.Builder
     */
    private static OkHttpClient.Builder buildOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
