package com.sismics.docs.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sismics.docs.R;
import com.sismics.docs.adapter.ShareListAdapter;
import com.sismics.docs.event.ShareDeleteEvent;
import com.sismics.docs.event.ShareSendEvent;
import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.resource.DocumentResource;
import com.sismics.docs.resource.ShareResource;
import com.sismics.docs.util.PreferenceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Document sharing dialog fragment.
 *
 * @author bgamard.
 */
public class DocShareFragment extends DialogFragment {
    /**
     * Document data.
     */
    private JSONObject document;

    /**
     * Document sharing dialog fragment.
     *
     * @param id Document ID
     */
    public static DocShareFragment newInstance(String id) {
        DocShareFragment fragment = new DocShareFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Setup the view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.document_share_dialog, null);
        final Button shareAddButton = (Button) view.findViewById(R.id.shareAddButton);
        final EditText shareNameEditText = (EditText) view.findViewById(R.id.shareNameEditText);

        // Add a share
        shareAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareNameEditText.setEnabled(false);
                shareAddButton.setEnabled(false);

                ShareResource.add(getActivity(), getArguments().getString("id"), shareNameEditText.getText().toString(),
                        new HttpCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        shareNameEditText.setText("");
                        loadShares(getDialog().getWindow().getDecorView());
                    }

                    @Override
                    public void onFailure(JSONObject json, Exception e) {
                        Toast.makeText(getActivity(), R.string.error_adding_share, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFinish() {
                        shareNameEditText.setEnabled(true);
                        shareAddButton.setEnabled(true);
                    }
                });
            }
        });

        // Get the shares
        loadShares(view);

        // Build the dialog
        builder.setView(view)
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });
        return builder.create();
    }

    /**
     * Load the shares.
     *
     * @param view View
     */
    private void loadShares(View view) {
        if (isDetached()) return;

        final ListView shareListView = (ListView) view.findViewById(R.id.shareListView);
        final TextView shareEmptyView = (TextView) view.findViewById(R.id.shareEmptyView);
        final ProgressBar shareProgressBar = (ProgressBar) view.findViewById(R.id.shareProgressBar);

        shareListView.setEmptyView(shareProgressBar);
        DocumentResource.get(getActivity(), getArguments().getString("id"), new HttpCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                document = response;
                JSONArray acls = response.optJSONArray("acls");
                shareProgressBar.setVisibility(View.GONE);
                shareListView.setEmptyView(shareEmptyView);
                shareListView.setAdapter(new ShareListAdapter(acls));
            }

            @Override
            public void onFailure(JSONObject json, Exception e) {
                getDialog().cancel();
                Toast.makeText(getActivity(), R.string.error_loading_shares, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * A share delete event has been fired.
     *
     * @param event Share delete event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ShareDeleteEvent event) {
        ShareResource.delete(getActivity(), event.getId(), new HttpCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                loadShares(getDialog().getWindow().getDecorView());
            }

            @Override
            public void onFailure(JSONObject json, Exception e) {
                Toast.makeText(getActivity(), R.string.error_deleting_share, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * A share send event has been fired.
     *
     * @param event Share send event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ShareSendEvent event) {
        if (document == null) return;

        // Build the share link
        String serverUrl = PreferenceUtil.getServerUrl(getActivity());
        String link = serverUrl + "/share.html#/share/" + document.optString("id") + "/" + event.getAcl().optString("id");

        // Build the intent
        Context context = getActivity();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, document.optString("title"));
        intent.putExtra(Intent.EXTRA_TEXT, link);
        intent.setType("text/plain");

        // Open the target chooser
        context.startActivity(Intent.createChooser(intent, context.getText(R.string.send_share_to)));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
