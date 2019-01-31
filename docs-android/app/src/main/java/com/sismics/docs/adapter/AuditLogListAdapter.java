package com.sismics.docs.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sismics.docs.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
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
     * Context.
     */
    private Context context;

    /**
     * Audit log list adapter.
     *
     * @param context Context
     * @param logs Logs
     */
    public AuditLogListAdapter(Context context, JSONArray logs) {
        this.context = context;
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
        StringBuilder message = new StringBuilder();

        // Translate entity name
        int stringId = context.getResources().getIdentifier("auditlog_" + log.optString("class"), "string", context.getPackageName());
        if (stringId == 0) {
            message.append(log.optString("class"));
        } else {
            message.append(context.getResources().getString(stringId));
        }
        message.append(" ");

        switch (log.optString("type")) {
            case "CREATE": message.append(context.getResources().getString(R.string.auditlog_created)); break;
            case "UPDATE": message.append(context.getResources().getString(R.string.auditlog_updated)); break;
            case "DELETE": message.append(context.getResources().getString(R.string.auditlog_deleted)); break;
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
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        TextView messageTextView = view.findViewById(R.id.messageTextView);
        TextView dateTextView = view.findViewById(R.id.dateTextView);
        usernameTextView.setText(log.optString("username"));
        messageTextView.setText(message);
        String date = DateFormat.getDateFormat(parent.getContext()).format(new Date(log.optLong("create_date")));
        dateTextView.setText(date);

        return view;
    }
}
