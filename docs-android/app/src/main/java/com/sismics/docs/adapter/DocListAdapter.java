package com.sismics.docs.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sismics.docs.R;
import com.sismics.docs.util.TagUtil;

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
        public ImageView sharedImageView;

        public ViewHolder(View v) {
            super(v);
            titleTextView = (TextView) v.findViewById(R.id.titleTextView);
            subtitleTextView = (TextView) v.findViewById(R.id.subtitleTextView);
            dateTextView = (TextView) v.findViewById(R.id.dateTextView);
            sharedImageView = (ImageView) v.findViewById(R.id.sharedImageView);
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
        holder.subtitleTextView.setText(TagUtil.buildSpannable(tags));

        String date = DateFormat.getDateFormat(holder.dateTextView.getContext()).format(new Date(document.optLong("create_date")));
        holder.dateTextView.setText(date);

        holder.sharedImageView.setVisibility(document.optBoolean("shared") ? View.VISIBLE : View.GONE);
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
     * Clear the documents.
     */
    public void clearDocuments() {
        documents = new JSONArray();
        notifyDataSetChanged();
    }

    /**
     * Add documents to display.
     *
     * @param documents Documents
     */
    public void addDocuments(JSONArray documents) {
        if (this.documents == null) {
            this.documents = new JSONArray();
        }

        for (int i = 0; i < documents.length(); i++) {
            this.documents.put(documents.optJSONObject(i));
        }

        notifyDataSetChanged();
    }
}