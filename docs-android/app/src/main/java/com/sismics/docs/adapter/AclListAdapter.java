package com.sismics.docs.adapter;

import android.content.Context;
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
 * ACL list adapter.
 *
 * @author bgamard.
 */
public class AclListAdapter extends BaseAdapter {
    /**
     * Shares.
     */
    private List<JSONObject> acls;

    /**
     * ACL list adapter.
     *
     * @param acls ACLs
     */
    public AclListAdapter(JSONArray acls) {
        this.acls = new ArrayList<>();

        // Extract only share ACLs
        for (int i = 0; i < acls.length(); i++) {
            JSONObject acl = acls.optJSONObject(i);
            this.acls.add(acl);
        }
    }

    @Override
    public int getCount() {
        return acls.size();
    }

    @Override
    public JSONObject getItem(int position) {
        return acls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).optString("id").hashCode();
    }

    @Override
    public View getView(int position, View view, final ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.acl_list_item, parent, false);
        }

        // Fill the view
        final JSONObject acl = getItem(position);
        TextView typeTextView = (TextView) view.findViewById(R.id.typeTextView);
        typeTextView.setText(acl.optString("type"));
        TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        nameTextView.setText(acl.optString("name"));
        TextView permTextView = (TextView) view.findViewById(R.id.permTextView);
        permTextView.setText(acl.optString("perm"));

        return view;
    }
}
