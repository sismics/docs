package com.sismics.docs.adapter;

import android.content.Context;
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
 * ACL list adapter.
 *
 * @author bgamard.
 */
public class AclListAdapter extends BaseAdapter {
    /**
     * Shares.
     */
    private List<AclItem> aclItemList;

    /**
     * ACL list adapter.
     *
     * @param acls ACLs
     */
    public AclListAdapter(JSONArray acls) {
        this.aclItemList = new ArrayList<>();

        // Group ACLs
        for (int i = 0; i < acls.length(); i++) {
            JSONObject acl = acls.optJSONObject(i);
            String type = acl.optString("type");
            String name = acl.optString("name");
            String perm = acl.optString("perm");

            boolean found = false;
            for (AclItem aclItem : aclItemList) {
                if (aclItem.type.equals(type) && aclItem.name.equals(name)) {
                    aclItem.permList.add(perm);
                    found = true;
                }
            }

            if (!found) {
                AclItem aclItem = new AclItem();
                aclItem.type = type;
                aclItem.name = name;
                aclItem.permList.add(perm);
                this.aclItemList.add(aclItem);
            }
        }
    }

    @Override
    public int getCount() {
        return aclItemList.size();
    }

    @Override
    public AclItem getItem(int position) {
        return aclItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View view, final ViewGroup parent) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.acl_list_item, parent, false);
        }

        // Fill the view
        final AclItem aclItem = getItem(position);
        TextView typeTextView = (TextView) view.findViewById(R.id.typeTextView);
        typeTextView.setText(aclItem.type);
        TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        nameTextView.setText(aclItem.name);
        TextView permTextView = (TextView) view.findViewById(R.id.permTextView);
        permTextView.setText(TextUtils.join(" + ", aclItem.permList));

        return view;
    }

    /**
     * An ACL item in the list.
     * Permissions are grouped together.
     */
    public static class AclItem {
        private String type;
        private String name;
        private List<String> permList = new ArrayList<>();

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return (type + name).hashCode();
        }
    }
}
