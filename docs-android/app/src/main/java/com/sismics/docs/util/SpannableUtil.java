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
 * Utility class for spannable.
 *
 * @author bgamard.
 */
public class SpannableUtil {
    /**
     * Create a colored spannable from tags.
     *
     * @param tags Tags
     * @return Colored spannable
     */
    public static Spannable buildSpannableTags(JSONArray tags) {
        return buildSpannable(tags, "name", "color");
    }

    /**
     * Create a spannable for contributors.
     *
     * @param contributors Contributors
     * @return Spannable
     */
    public static Spannable buildSpannableContributors(JSONArray contributors) {
        return buildSpannable(contributors, "username", null);
    }

    /**
     * Create a spannable for relations.
     *
     * @param relations Relations
     * @return Spannable
     */
    public static Spannable buildSpannableRelations(JSONArray relations) {
        return buildSpannable(relations, "title", null);
    }

    /**
     * Create a spannable from a JSONArray.
     *
     * @param array JSONArray
     * @param valueName Name of the value part
     * @param colorName Name of the color part (optional)
     * @return Spannable
     */
    private static Spannable buildSpannable(JSONArray array, String valueName, String colorName) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (int i = 0; i < array.length(); i++) {
            final JSONObject tag = array.optJSONObject(i);
            int start = builder.length();
            builder.append(" ").append(tag.optString(valueName)).append(" ");
            builder.setSpan(new ForegroundColorSpan(Color.WHITE), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new BackgroundColorSpan(Color.parseColor(tag.optString(colorName, "#5bc0de"))), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            /*
            TODO : Make tags, relations and contributors clickable
            builder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.WHITE);
                    ds.setUnderlineText(false);
                }
            }, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
            builder.append(" ");
        }

        return builder;
    }
}
