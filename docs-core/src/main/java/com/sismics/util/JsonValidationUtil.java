package com.sismics.util;

import org.codehaus.jackson.JsonNode;

import java.text.MessageFormat;

/**
 * JSON validation utilities.
 *
 * @author jtremeaux
 */
public class JsonValidationUtil {

    /**
     * Checks if the JSON node contains the properties (not null).
     * 
     * @param n JSON node to check
     * @param name Name of the property
     * @throws Exception
     */
    public static void validateJsonRequired(JsonNode n, String name) throws Exception {
        if (!n.has(name)) {
            throw new Exception(MessageFormat.format("{0} must be set", name));
        }
    }

    /**
     * Checks that the property is a JSON object.
     * 
     * @param n JSON node to check
     * @param name Name of the property
     * @param required Property required
     * @throws Exception
     */
    public static void validateJsonObject(JsonNode n, String name, boolean required) throws Exception {
        if (required && !n.has(name)) {
            throw new Exception(MessageFormat.format("{0} must be set", name));
        }
        if (n.has(name) && !n.path(name).isObject()) {
            throw new Exception(MessageFormat.format("{0} must be a JSON object", name));
        }
    }

    /**
     * Checks that the property is a number.
     * 
     * @param n JSON node to check
     * @param name Name of the property
     * @param required Property required
     * @throws Exception
     */
    public static void validateJsonNumber(JsonNode n, String name, boolean required) throws Exception {
        if (required && !n.has(name)) {
            throw new Exception(MessageFormat.format("{0} must be set", name));
        }
        if (n.has(name) && !n.path(name).isNumber()) {
            throw new Exception(MessageFormat.format("{0} must be a number", name));
        }
    }

    /**
     * Checks that the property is a long.
     * 
     * @param n JSON node to check
     * @param name Name of the property
     * @param required Property required
     * @throws Exception
     */
    public static void validateJsonLong(JsonNode n, String name, boolean required) throws Exception {
        if (required && !n.has(name)) {
            throw new Exception(MessageFormat.format("{0} must be set", name));
        }
        if (n.has(name) && !n.path(name).isLong()) {
            throw new Exception(MessageFormat.format("{0} must be a long", name));
        }
    }

    /**
     * Checks that the property is a string.
     * 
     * @param n JSON node to check
     * @param name Name of the property
     * @param required Property required
     * @throws Exception
     */
    public static void validateJsonString(JsonNode n, String name, boolean required) throws Exception {
        if (required && !n.has(name)) {
            throw new Exception(MessageFormat.format("{0} must be set", name));
        }
        if (n.has(name) && !n.path(name).isTextual()) {
            throw new Exception(MessageFormat.format("{0} must be a string", name));
        }
    }

    /**
     * Checks that the property is an array.
     * 
     * @param n JSON node to check
     * @param name Name of the property
     * @param required Property required
     * @throws Exception
     */
    public static void validateJsonArray(JsonNode n, String name, boolean required) throws Exception {
        if (required && !n.has(name)) {
            throw new Exception(MessageFormat.format("{0} must be set", name));
        }
        if (n.has(name) && !n.path(name).isArray()) {
            throw new Exception(MessageFormat.format("{0} must be an array", name));
        }
    }
}
