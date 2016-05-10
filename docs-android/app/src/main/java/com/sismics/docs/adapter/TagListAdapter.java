package com.sismics.docs.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sismics.docs.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tag list adapter.
 *
 * @author bgamard.
 */
public class TagListAdapter extends BaseAdapter {
    /**
     * Tags.
     */
    private List<TagItem> tagItemList = new ArrayList<>();

    /**
     * Tag list adapter.
     *
     * @param tagsArray Tags
     */
    public TagListAdapter(JSONArray tagsArray) {
        List<JSONObject> tags = new ArrayList<>();
        for (int i = 0; i < tagsArray.length(); i++) {
            tags.add(tagsArray.optJSONObject(i));
        }

        // Reorder tags by parent/child relation and compute depth
        int depth = 0;
        initTags(tags, "", depth);
    }

    /**
     * Init tags model recursively.
     *
     * @param tags All tags from server
     * @param parentId Parent ID
     * @param depth Depth
     */
    private void initTags(List<JSONObject> tags, String parentId, int depth) {
        // Get all tags with this parent
        for (JSONObject tag : tags) {
            String tagParentId = tag.optString("parent");
            if (parentId.equals(tagParentId)) {
                TagItem tagItem = new TagItem();
                tagItem.id = tag.optString("id");
                tagItem.name = tag.optString("name");
                tagItem.color = tag.optString("color");
                tagItem.depth = depth;
                tagItemList.add(tagItem);
                initTags(tags, tagItem.id, depth + 1);
            }
        }
    }

    @Override
    public int getCount() {
        return tagItemList.size();
    }

    @Override
    public TagItem getItem(int position) {
        return tagItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id.hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.tag_list_item, parent, false);
        }

        // Fill the view
        TagItem tagItem = getItem(position);
        TextView tagTextView = (TextView) view.findViewById(R.id.tagTextView);
        tagTextView.setText(tagItem.name);

        // Label color filtering
        ImageView labelImageView = (ImageView) view.findViewById(R.id.labelImageView);
        Drawable labelDrawable = labelImageView.getDrawable().mutate();
        labelDrawable.setColorFilter(Color.parseColor(tagItem.color), PorterDuff.Mode.MULTIPLY);
        labelImageView.setImageDrawable(labelDrawable);
        labelImageView.invalidate();

        // Offset according to depth
        Resources resources = parent.getContext().getResources();
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) labelImageView.getLayoutParams();
        layoutParams.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, tagItem.depth * 12, resources.getDisplayMetrics());
        labelImageView.setLayoutParams(layoutParams);
        labelImageView.requestLayout();

        return view;
    }

    /**
     * A tag item in the tags list.
     */
    public static class TagItem {
        private String id;
        private String name;
        private String color;
        private int depth;

        public String getName() {
            return name;
        }
    }
}
