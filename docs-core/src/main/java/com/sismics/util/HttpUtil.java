package com.sismics.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * HTTP request utilities.
 * 
 * @author jtremeaux
 */
public class HttpUtil {
    /**
     * Format of the expires header.
     */
    private static final SimpleDateFormat EXPIRES_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    /**
     * Build an Expires HTTP header.
     *
     * @param futureTime Expire interval
     * @return Formatted header value
     */
    public static String buildExpiresHeader(long futureTime) {
        return EXPIRES_FORMAT.format(new Date().getTime() + futureTime);
    }
}
