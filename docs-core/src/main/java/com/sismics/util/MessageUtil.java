package com.sismics.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Messages utilities.
 *
 * @author jtremeaux
 */
public class MessageUtil {
    /**
     * Returns a localized message in the specified language.
     * Returns **key** if no message exists for this key.
     * 
     * @param locale Locale
     * @param key Message key
     * @param args Arguments to format
     * @return Formatted message
     */
    public static String getMessage(Locale locale, String key, Object... args) {
        ResourceBundle resources = ResourceBundle.getBundle("messages", locale);
        String message;
        try {
            message = resources.getString(key);
        } catch (MissingResourceException e) {
            message = "**" + key + "**";
        }
        return MessageFormat.format(message, args);
    }

    /**
     * Returns the resource bundle corresponding to the specified language.
     * 
     * @param locale Locale
     * @return Resource bundle
     */
    public static ResourceBundle getMessage(Locale locale) {
        return ResourceBundle.getBundle("messages", locale);
    }
}
