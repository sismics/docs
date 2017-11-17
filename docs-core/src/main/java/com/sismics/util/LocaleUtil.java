package com.sismics.util;

import com.google.common.base.Strings;

import java.util.Locale;

/**
 * Locale utilities.
 *
 * @author jtremeaux
 */
public class LocaleUtil {
    /**
     * Returns a locale from the language / country / variation code (ex: fr_FR).
     * 
     * @param localeCode Locale code
     * @return Locale instance
     */
    public static Locale getLocale(String localeCode) {
        if (Strings.isNullOrEmpty(localeCode)) {
            return Locale.ENGLISH;
        }

        String[] localeCodeArray = localeCode.split("_");
        String language = localeCodeArray[0];
        String country = "";
        String variant = "";
        if (localeCodeArray.length >= 2) {
            country = localeCodeArray[1];
        }
        if (localeCodeArray.length >= 3) {
            variant = localeCodeArray[2];
        }
        return new Locale(language, country, variant);
    }
}
