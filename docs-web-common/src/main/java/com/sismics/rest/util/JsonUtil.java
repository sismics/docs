package com.sismics.rest.util;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * JSON utilities.
 * 
 * @author jtremeaux
 */
public class JsonUtil {
    
    /**
     * Fix of {@see JsonObject.append()}, which seems to create nested arrays.
     * 
     * @param o JSON Object
     * @param key Key containing the array of null
     * @param value Value to append
     * @return Updated object
     * @throws JSONException
     */
    public static JSONObject append(JSONObject o, String key, JSONObject value)  throws JSONException {
        Object prevValue = o.opt(key);
        if (prevValue == null) {
            o.put(key, new JSONArray().put(value));
        } else if (!(prevValue instanceof JSONArray)){
            throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
        } else {
            JSONArray newArray = new JSONArray();
            JSONArray oldArray = ((JSONArray) prevValue);
            for (int i = 0; i < oldArray.length(); i++) {
                newArray.put(oldArray.get(i));
            }
            newArray.put(value);
            o.put(key, newArray);
        }
        return o;
    }
}
