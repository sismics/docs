package com.sismics.docs.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.sismics.docs.resource.cookie.PersistentCookieStore;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.cert.CertificateException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
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
     * OkHttp singleton client.
     */
    private static OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * Singleton cache.
     */
    private static Cache cache = null;

    /**
     * User-Agent to use.
     */
    protected static String userAgent = null;

    /**
     * Accept-Language header.
     */
    protected static String acceptLanguage = null;

    static {
        // OkHttp configuration
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
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Configure OkHttpClient
            okHttpClient = okHttpClient.newBuilder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .sslSocketFactory(sslSocketFactory)
                    .build();
        } catch (Exception e) {
            // NOP
        }
    }

    /**
     * Build a Picasso object with base config.
     *
     * @param context Context
     * @return Picasso object
     */
    public static Picasso picasso(Context context) {
        OkHttpClient okHttpClient = buildClient(context)
                .newBuilder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException { // Override cache configuration
                        final Request original = chain.request();
                        return chain.proceed(original.newBuilder()
                                .header("Cache-Control", "max-age=" + (3600 * 24 * 365))
                                .method(original.method(), original.body())
                                .build());
                    }
                })
                .cache(getCache(context))
                .build();

        Picasso picasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
        picasso.setIndicatorsEnabled(false); // Debug stuff
        return picasso;
    }

    /**
     * Get and eventually build the singleton cache.
     *
     * @param context Context
     * @return Cache
     */
    private static Cache getCache(Context context) {
        if (cache == null) {
            cache = new Cache(context.getCacheDir(),
                    PreferenceUtil.getIntegerPreference(context, PreferenceUtil.PREF_CACHE_SIZE, 0) * 1000000);
        }
        return cache;
    }

    /**
     * Clear the HTTP cache.
     *
     * @param context Context
     */
    public static void clearCache(Context context) {
        Cache cache = getCache(context);
        try {
            cache.evictAll();
        } catch (IOException e) {
            Log.e("OKHttpUtil", "Error clearing cache", e);
        }
    }

    /**
     * Build an OkHttpClient.
     *
     * @param context Context
     * @return OkHttpClient
     */
    public static OkHttpClient buildClient(final Context context) {
        // One-time header computation
        if (userAgent == null) {
            userAgent = "Teedy Android " + ApplicationUtil.getVersionName(context) + "/Android " + Build.VERSION.RELEASE + "/" + Build.MODEL;
        }

        if (acceptLanguage == null) {
            Locale locale = Locale.getDefault();
            acceptLanguage = locale.getLanguage() + "_" + locale.getCountry();
        }

        // Cookie handling
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        CookieManager cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);

        // Runtime configuration
        return okHttpClient.newBuilder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        return chain.proceed(original.newBuilder()
                                .header("User-Agent", userAgent)
                                .header("Accept-Language", acceptLanguage)
                                .method(original.method(), original.body())
                                .build());
                    }
                })
                .build();
    }
}
