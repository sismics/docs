package com.sismics.docs.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sismics.docs.R;
import com.tokenautocomplete.TokenCompleteTextView;

import org.json.JSONObject;

/**
 * Auto-complete text view displaying tags.
 *
 * @author bgamard
 */
public class TagsCompleteTextView extends TokenCompleteTextView {
    public TagsCompleteTextView(Context context) {
        super(context);
        init();
    }

    public TagsCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TagsCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setFilters(new InputFilter[] {});
    }

    @Override
    protected View getViewForObject(Object object) {
        JSONObject tag = (JSONObject) object;

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.tag_autocomplete_token, (ViewGroup) getParent(), false);
        TextView textView = (TextView) view.findViewById(R.id.tagTextView);
        textView.setText(tag.optString("name"));

        Drawable drawable = textView.getCompoundDrawables()[0].mutate();
        drawable.setColorFilter(Color.parseColor(tag.optString("color")), PorterDuff.Mode.MULTIPLY);
        textView.setCompoundDrawables(drawable, null, null, null);
        textView.invalidate();

        return view;
    }

    @Override
    protected Object defaultObject(String completionText) {
        return completionText;
    }
}