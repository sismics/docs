package com.sismics.docs.ui.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sismics.docs.R;
import com.tokenautocomplete.TokenCompleteTextView;

public class TagsCompleteTextView extends TokenCompleteTextView {
    public TagsCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(Object object) {
        String p = (String)object;

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        TextView view = (TextView) inflater.inflate(R.layout.tag_complete_item, (ViewGroup) getParent(), false);
        view.setText(p);

        return view;
    }

    @Override
    protected Object defaultObject(String completionText) {
        return completionText;
    }
}