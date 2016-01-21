package com.sismics.docs.resource;

import android.content.Context;
import android.os.Build;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.sismics.docs.util.ApplicationUtil;
import com.sismics.docs.util.PreferenceUtil;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.Socket;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Base class for API access.
 * 
 * @author bgamard
 */
public class BaseResource {
    
    /**
     * User-Agent to use.
     */
    protected static String USER_AGENT = null;
    
    /**
     * Accept-Language header.
     */
    protected static String ACCEPT_LANGUAGE = null;
    
    /**
     * Async HTTP client.
     */
    protected static AsyncHttpClient client = new AsyncHttpClient();

    /**
     * OkHttp client.
     */
    protected static OkHttpClient okHttpClient = new OkHttpClient();

    static {
        // 20sec default timeout
        client.setTimeout(60000);
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            // Async HTTP Client uses another HTTP libary
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(sf);
        } catch (Exception e) {
            // NOP
        }

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
                    .hostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                    .sslSocketFactory(sslSocketFactory)
                    .build();
        } catch (Exception e) {
            // NOP
        }
    }
    
    /**
     * Resource initialization.
     *
     * @param context Context
     */
    protected static void init(Context context) {
        client.setCookieStore(new PersistentCookieStore(context));
        
        if (USER_AGENT == null) {
            USER_AGENT = "Sismics Docs Android " + ApplicationUtil.getVersionName(context) + "/Android " + Build.VERSION.RELEASE + "/" + Build.MODEL;
            client.setUserAgent(USER_AGENT);
        }
        
        if (ACCEPT_LANGUAGE == null) {
            Locale locale = Locale.getDefault();
            ACCEPT_LANGUAGE = locale.getLanguage() + "_" + locale.getCountry();
            client.addHeader("Accept-Language", ACCEPT_LANGUAGE);
        }
    }

    /**
     * Build an OkHttpClient.
     *
     * @param context Context
     * @return OkHttpClient
     */
    protected static OkHttpClient buildOkHttpClient(final Context context) {
        // Header computation
        if (USER_AGENT == null) {
            USER_AGENT = "Sismics Docs Android " + ApplicationUtil.getVersionName(context) + "/Android " + Build.VERSION.RELEASE + "/" + Build.MODEL;
        }

        if (ACCEPT_LANGUAGE == null) {
            Locale locale = Locale.getDefault();
            ACCEPT_LANGUAGE = locale.getLanguage() + "_" + locale.getCountry();
        }

        // Cookie handling
        com.sismics.docs.resource.cookie.PersistentCookieStore cookieStore = new com.sismics.docs.resource.cookie.PersistentCookieStore(context);
        CookieManager cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);
        cookieStore.add(URI.create(PreferenceUtil.getServerUrl(context)),
                new HttpCookie("auth_token", PreferenceUtil.getAuthToken(context))); // TODO Remove me when async http is ditched

        // Runtime configuration
        return okHttpClient.newBuilder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        return chain.proceed(originalRequest.newBuilder()
                                .header("User-Agent", USER_AGENT)
                                .header("Accept-Language", ACCEPT_LANGUAGE)
                                // TODO necessary?? .method(originalRequest.method(), originalRequest.body())
                                .build());
                    }
                })
                .build();
    }

    /**
     * Socket factory to allow self-signed certificates for Async HTTP Client.
     *
     * @author bgamard
     */
    public static class MySSLSocketFactory extends cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    /**
     * Returns cleaned API URL.
     *
     * @param context Context
     * @return Cleaned API URL
     */
    protected static String getApiUrl(Context context) {
        String serverUrl = PreferenceUtil.getServerUrl(context);
        
        if (serverUrl == null) {
            return null;
        }
        
        return serverUrl + "/api";
    }
}
