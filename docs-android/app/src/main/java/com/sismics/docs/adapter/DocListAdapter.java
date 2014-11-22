package com.sismics.docs.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sismics.docs.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

/**
 * Adapter of documents.
 *
 * @author bgamard
 */
public class DocListAdapter extends RecyclerView.Adapter<DocListAdapter.ViewHolder> {
    /**
     * Displayed documents.
     */
    private JSONArray documents;

    /**
     * ViewHolder.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView subtitleTextView;
        public TextView dateTextView;

        public ViewHolder(View v) {
            super(v);
            titleTextView = (TextView) v.findViewById(R.id.titleTextView);
            subtitleTextView = (TextView) v.findViewById(R.id.subtitleTextView);
            dateTextView = (TextView) v.findViewById(R.id.dateTextView);
        }
    }

    public DocListAdapter() {
    }

    @Override
    public DocListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.doc_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject document = documents.optJSONObject(position);

        holder.titleTextView.setText(document.optString("title"));

        JSONArray tags = document.optJSONArray("tags");
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < tags.length(); i++) {
            JSONObject tag = tags.optJSONObject(i);
            int start = builder.length();
            builder.append(" ").append(tag.optString("name")).append(" ");
            builder.setSpan(new ForegroundColorSpan(Color.WHITE), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new BackgroundColorSpan(Color.parseColor(tag.optString("color"))), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(" ");
        }
        holder.subtitleTextView.setText(builder);

        String date = DateFormat.getDateFormat(holder.dateTextView.getContext()).format(new Date(document.optLong("create_date")));
        holder.dateTextView.setText(date);
    }

    @Override
    public int getItemCount() {
        if (documents == null) {
            return 0;
        }
        return documents.length();
    }

    /**
     * Return an item at a given position.
     *
     * @param position Item position
     * @return Item
     */
    public JSONObject getItemAt(int position) {
        if (documents == null) {
            return null;
        }

        return documents.optJSONObject(position);
    }

    /**
     * Update the displayed documents.
     * @param documents Documents
     */
    public void setDocuments(JSONArray documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }
}