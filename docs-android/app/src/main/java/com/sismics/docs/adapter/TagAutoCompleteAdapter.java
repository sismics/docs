package com.sismics.docs.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sismics.docs.R;
import com.tokenautocomplete.FilteredArrayAdapter;

import org.json.JSONObject;

import java.util.List;

/**
 * Tag auto-complete adapter.
 *
 * @author bgamard.
 */
public class TagAutoCompleteAdapter extends FilteredArrayAdapter<JSONObject> {
    public TagAutoCompleteAdapter(Context context, int resource, List<JSONObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.tag_autocomplete_item, parent, false);
        }

        // Fill the view
        JSONObject tag = getItem(position);
        TextView textView = (TextView) view;
        textView.setText(tag.optString("name"));

        Drawable drawable = textView.getCompoundDrawables()[0].mutate();
        drawable.setColorFilter(Color.parseColor(tag.optString("color")), PorterDuff.Mode.MULTIPLY);
        textView.setCompoundDrawables(drawable, null, null, null);
        textView.invalidate();

        return view;
    }

    @Override
    protected boolean keepObject(JSONObject tag, String s) {
        return tag.optString("name").toLowerCase().startsWith(s.toLowerCase());
    }
}
