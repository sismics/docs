package com.sismics.docs.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sismics.docs.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Tag list adapter.
 *
 * @author bgamard.
 */
public class TagListAdapter extends BaseAdapter {
    /**
     * Tags.
     */
    private JSONArray tags;

    /**
     * Tag list adapter.
     *
     * @param tags Tags
     */
    public TagListAdapter(JSONArray tags) {
        this.tags = tags;
    }

    @Override
    public int getCount() {
        return tags.length();
    }

    @Override
    public JSONObject getItem(int position) {
        return tags.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).optString("id").hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.tag_list_item, parent, false);
        }

        // Fill the view
        JSONObject tag = getItem(position);
        TextView tagTextView = (TextView) view.findViewById(R.id.tagTextView);
        tagTextView.setText(tag.optString("name"));
        TextView tagCountTextView = (TextView) view.findViewById(R.id.tagCountTextView);
        tagCountTextView.setText(tag.optString("count"));

        // Label color filtering
        ImageView labelImageView = (ImageView) view.findViewById(R.id.labelImageView);
        Drawable labelDrawable = labelImageView.getDrawable().mutate();
        labelDrawable.setColorFilter(Color.parseColor(tag.optString("color")), PorterDuff.Mode.MULTIPLY);
        labelImageView.setImageDrawable(labelDrawable);
        labelImageView.invalidate();

        return view;
    }
}
