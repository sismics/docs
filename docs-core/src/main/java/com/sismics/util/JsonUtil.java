package com.sismics.util;

import javax.json.Json;
import javax.json.JsonValue;

/**
 * JSON utilities.
 * 
 * @author bgamard
 */
public class JsonUtil {
    /**
     * Returns a JsonValue from a String.
     * 
     * @param value Value
     * @return JsonValue
     */
    public static JsonValue nullable(String value) {
        if (value == null) {
            return JsonValue.NULL;
        }
        return Json.createObjectBuilder().add("_", value).build().get("_");
    }
    
    /**
     * Returns a JsonValue from an Integer.
     * 
     * @param value Value
     * @return JsonValue
     */
    public static JsonValue nullable(Integer value) {
        if (value == null) {
            return JsonValue.NULL;
        }
        return Json.createObjectBuilder().add("_", value).build().get("_");
    }

    /**
     * Returns a JsonValue from an Long.
     *
     * @param value Value
     * @return JsonValue
     */
    public static JsonValue nullable(Long value) {
        if (value == null) {
            return JsonValue.NULL;
        }
        return Json.createObjectBuilder().add("_", value).build().get("_");
    }
}
