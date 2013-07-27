package com.sismics.rest.util;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.sismics.docs.core.dao.file.theme.ThemeDao;
import com.sismics.docs.core.dao.jpa.LocaleDao;
import com.sismics.docs.core.model.jpa.Locale;
import com.sismics.rest.exception.ClientException;

/**
 * Utility class to validate parameters.
 *
 * @author jtremeaux
 */
public class ValidationUtil {
    private static Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");
    
    private static Pattern HTTP_URL_PATTERN = Pattern.compile("https?://.+");
    
    private static Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9_]+");
    
    /**
     * Checks that the argument is not null.
     * 
     * @param s Object tu validate
     * @param name Name of the parameter
     * @throws JSONException
     */
    public static void validateRequired(Object s, String name) throws JSONException {
        if (s == null) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
        }
    }
    
    /**
     * Validate a string length.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @param lengthMin Minimum length (or null)
     * @param lengthMax Maximum length (or null)
     * @param nullable True if the string can be empty or null
     * @return String without white spaces
     * @throws ClientException
     */
    public static String validateLength(String s, String name, Integer lengthMin, Integer lengthMax, boolean nullable) throws JSONException {
        s = StringUtils.strip(s);
        if (nullable && StringUtils.isEmpty(s)) {
            return s;
        }
        if (s == null) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
        }
        if (lengthMin != null && s.length() < lengthMin) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be more than {1} characters", name, lengthMin));
        }
        if (lengthMax != null && s.length() > lengthMax) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be more than {1} characters", name, lengthMax));
        }
        return s;
    }
    
    /**
     * Validate a string length. The string mustn't be empty.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @param lengthMin Minimum length (or null)
     * @param lengthMax Maximum length (or null)
     * @return String without white spaces
     * @throws ClientException
     */
    public static String validateLength(String s, String name, Integer lengthMin, Integer lengthMax) throws JSONException {
        return validateLength(s, name, lengthMin, lengthMax, false);
    }
    
    /**
     * Checks if the string is not null and is not only whitespaces.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @return String without white spaces
     * @throws JSONException
     */
    public static String validateStringNotBlank(String s, String name) throws JSONException {
        return validateLength(s, name, 1, null, false);
    }
    
    /**
     * Checks if the string is an email.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @throws JSONException
     */
    public static void validateEmail(String s, String name) throws JSONException {
        if (!EMAIL_PATTERN.matcher(s).matches()) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be an email", name));
        }
    }
    
    /**
     * Validates that the provided string matches an URL with HTTP or HTTPS scheme.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @return Stripped URL
     * @throws JSONException
     */
    public static String validateHttpUrl(String s, String name) throws JSONException {
        s = StringUtils.strip(s);
        if (!HTTP_URL_PATTERN.matcher(s).matches()) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be an HTTP(s) URL", name));
        }
        return s;
    }
    
    /**
     * Checks if the string uses only alphanumerical or underscore characters.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @throws JSONException
     */
    public static void validateAlphanumeric(String s, String name) throws JSONException {
        if (!ALPHANUMERIC_PATTERN.matcher(s).matches()) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must have only alphanumeric or underscore characters", name));
        }
    }
    
    /**
     * Validates and parses a date.
     * 
     * @param s String to validate
     * @param name Name of the parameter
     * @param nullable True if the string can be empty or null
     * @return Parsed date
     * @throws JSONException
     */
    public static Date validateDate(String s, String name, boolean nullable) throws JSONException {
        if (Strings.isNullOrEmpty(s)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
            } else {
                return null;
            }
        }
        try {
            return new DateTime(Long.parseLong(s)).toDate();
        } catch (NumberFormatException e) {
            throw new ClientException("ValidationError", MessageFormat.format("{0} must be a date", name));
        }
    }

    /**
     * Validates a locale.
     * 
     * @param localeId String to validate
     * @param name Name of the parameter
     * @return String without white spaces
     * @param nullable True if the string can be empty or null
     * @throws ClientException
     */
    public static String validateLocale(String localeId, String name, boolean nullable) throws JSONException {
        localeId = StringUtils.strip(localeId);
        if (StringUtils.isEmpty(localeId)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("{0} is required", name));
            } else {
                return null;
            }
        }
        LocaleDao localeDao = new LocaleDao();
        Locale locale = localeDao.getById(localeId);
        if (locale == null) {
            throw new ClientException("ValidationError", "Locale not found: " + localeId);
        }
        return localeId;
    }

    /**
     * Validates a theme.
     * 
     * @param themeId ID of the theme to validate
     * @param name Name of the parameter
     * @return String without white spaces
     * @param nullable True if the string can be empty or null
     * @throws ClientException
     */
    public static String validateTheme(String themeId, String name, boolean nullable) throws JSONException {
        themeId = StringUtils.strip(themeId);
        if (StringUtils.isEmpty(themeId)) {
            if (!nullable) {
                throw new ClientException("ValidationError", MessageFormat.format("{0} is required", name));
            } else {
                return null;
            }
        }
        ThemeDao themeDao = new ThemeDao();
        List<String> themeList = themeDao.findAll();
        if (!themeList.contains(themeId)) {
            throw new ClientException("ValidationError", "Theme not found: " + themeId);
        }
        return themeId;
    }
}
