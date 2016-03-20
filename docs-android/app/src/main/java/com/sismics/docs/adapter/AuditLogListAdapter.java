package com.sismics.docs.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sismics.docs.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Audit log list adapter.
 *
 * @author bgamard.
 */
public class AuditLogListAdapter extends BaseAdapter {
    /**
     * Shares.
     */
    private List<JSONObject> logList;

    /**
     * Audit log list adapter.
     *
     * @param logs Logs
     */
    public AuditLogListAdapter(JSONArray logs) {
        this.logList = new ArrayList<>();

        for (int i = 0; i < logs.length(); i++) {
            logList.add(logs.optJSONObject(i));
        }
    }

    @Override
    public int getCount() {
        return logList.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return logList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View view, final ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.auditlog_list_item, parent, false);
        }

        // Build message
        final JSONObject log = getItem(position);
        StringBuilder message = new StringBuilder(log.optString("class"));
        switch (log.optString("type")) {
            case "CREATE": message.append(" created"); break;
            case "UPDATE": message.append(" updated"); break;
            case "DELETE": message.append(" deleted"); break;
        }
        switch (log.optString("class")) {
            case "Document":
            case "Acl":
            case "Tag":
            case "User":
            case "Group":
                message.append(" : ");
                message.append(log.optString("message"));
                break;
        }

        // Fill the view
        TextView usernameTextView = (TextView) view.findViewById(R.id.usernameTextView);
        TextView messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        usernameTextView.setText(log.optString("username"));
        messageTextView.setText(message);

        return view;
    }
}
