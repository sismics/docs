package com.sismics.docs.util;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class for tags.
 *
 * @author bgamard.
 */
public class TagUtil {
    /**
     * Create a colored spannable from tags.
     *
     * @param tags Tags
     * @return Colored spannable
     */
    public static Spannable buildSpannable(JSONArray tags) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (int i = 0; i < tags.length(); i++) {
            JSONObject tag = tags.optJSONObject(i);
            int start = builder.length();
            builder.append(" ").append(tag.optString("name")).append(" ");
            builder.setSpan(new ForegroundColorSpan(Color.WHITE), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new BackgroundColorSpan(Color.parseColor(tag.optString("color"))), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ");
        }

        return builder;
    }
}
