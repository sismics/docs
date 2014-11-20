package com.sismics.docs.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sismics.docs.R;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Adapter of documents.
 *
 * @author bgamard
 */
public class DocListAdapter extends RecyclerView.Adapter<DocListAdapter.ViewHolder> {
    private JSONArray documents;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView titleTextView;
        public TextView subtitleTextView;
        public ViewHolder(View v) {
            super(v);
            titleTextView = (TextView) v.findViewById(R.id.titleTextView);
            subtitleTextView = (TextView) v.findViewById(R.id.subtitleTextView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public DocListAdapter() {
    }

    public void setDocuments(JSONArray documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DocListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.doc_list_item, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        JSONObject document = documents.optJSONObject(position);
        holder.titleTextView.setText(document.optString("title"));
        holder.subtitleTextView.setText(document.optString("description"));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (documents == null) {
            return 0;
        }
        return documents.length();
    }
}