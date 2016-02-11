package com.sismics.docs.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sismics.docs.R;
import com.sismics.docs.event.ShareDeleteEvent;
import com.sismics.docs.event.ShareSendEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Share list adapter.
 *
 * @author bgamard.
 */
public class ShareListAdapter extends BaseAdapter {
    /**
     * Shares.
     */
    private List<JSONObject> acls;

    /**
     * Share list adapter.
     *
     * @param acls ACLs
     */
    public ShareListAdapter(JSONArray acls) {
        this.acls = new ArrayList<>();

        // Extract only share ACLs
        for (int i = 0; i < acls.length(); i++) {
            JSONObject acl = acls.optJSONObject(i);
            if (acl.optString("type").equals("SHARE")) {
                this.acls.add(acl);
            }
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
            view = vi.inflate(R.layout.share_list_item, parent, false);
        }

        // Fill the view
        final JSONObject acl = getItem(position);
        String name = acl.optString("name");
        TextView shareTextView = (TextView) view.findViewById(R.id.shareTextView);
        shareTextView.setText(name.isEmpty() ? parent.getContext().getString(R.string.share_default_name) : name);

        // Delete a share
        ImageButton shareDeleteButton = (ImageButton) view.findViewById(R.id.shareDeleteButton);
        shareDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ShareDeleteEvent(acl.optString("id")));
            }
        });

        // Send the link
        ImageButton shareSendButton = (ImageButton) view.findViewById(R.id.shareSendButton);
        shareSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ShareSendEvent(acl));
            }
        });

        return view;
    }
}
