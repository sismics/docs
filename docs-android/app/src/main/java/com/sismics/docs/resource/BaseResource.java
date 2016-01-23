package com.sismics.docs.resource;

import android.content.Context;
import android.os.Build;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.sismics.docs.util.ApplicationUtil;
import com.sismics.docs.util.PreferenceUtil;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;

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
