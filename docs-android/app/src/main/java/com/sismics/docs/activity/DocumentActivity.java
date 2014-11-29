package com.sismics.docs.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sismics.docs.R;
import com.sismics.docs.adapter.FilePagerAdapter;
import com.sismics.docs.event.DocumentFullscreenEvent;
import com.sismics.docs.fragment.DocShareFragment;
import com.sismics.docs.listener.JsonHttpResponseHandler;
import com.sismics.docs.model.application.ApplicationContext;
import com.sismics.docs.resource.FileResource;
import com.sismics.docs.util.PreferenceUtil;
import com.sismics.docs.util.TagUtil;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Document activity.
 * 
 * @author bgamard
 */
public class DocumentActivity extends ActionBarActivity {
    /**
     * File view pager.
     */
    ViewPager fileViewPager;

    /**
     * File pager adapter.
     */
    FilePagerAdapter filePagerAdapter;

    /**
     * Document displayed.
     */
    JSONObject document;

    @Override
    protected void onCreate(final Bundle args) {
        super.onCreate(args);

        // Check if logged in
        if (!ApplicationContext.getInstance().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Handle activity context
        if (getIntent() == null) {
            finish();
            return;
        }

        // Parse input document
        String documentJson = getIntent().getStringExtra("document");
        if (documentJson == null) {
            finish();
            return;
        }

        try {
            document = new JSONObject(documentJson);
        } catch (JSONException e) {
            finish();
            return;
        }

        // Setup the activity
        setContentView(R.layout.document_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Grab the document
        refreshDocument(document);

        EventBus.getDefault().register(this);
    }

    /**
     * Refresh the displayed document.
     *
     * @param document Document in JSON format
     */
    private void refreshDocument(JSONObject document) {
        String id = document.optString("id");
        String title = document.optString("title");
        String date = DateFormat.getDateFormat(this).format(new Date(document.optLong("create_date")));
        String description = document.optString("description");
        boolean shared = document.optBoolean("shared");
        String language = document.optString("language");
        JSONArray tags = document.optJSONArray("tags");

        // Fill the layout
        setTitle(title);
        TextView createdDateTextView = (TextView) findViewById(R.id.createdDateTextView);
        createdDateTextView.setText(date);

        TextView descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        if (description == null || description.isEmpty()) {
            descriptionTextView.setVisibility(View.GONE);
        } else {
            descriptionTextView.setText(description);
        }

        TextView tagTextView = (TextView) findViewById(R.id.tagTextView);
        if (tags.length() == 0) {
            tagTextView.setVisibility(View.GONE);
        } else {
            tagTextView.setText(TagUtil.buildSpannable(tags));
        }

        ImageView languageImageView = (ImageView) findViewById(R.id.languageImageView);
        languageImageView.setImageResource(getResources().getIdentifier(language, "drawable", getPackageName()));

        ImageView sharedImageView = (ImageView) findViewById(R.id.sharedImageView);
        sharedImageView.setVisibility(shared ? View.VISIBLE : View.GONE);

        // Grab the attached files
        final View progressBar = findViewById(R.id.progressBar);
        final TextView filesEmptyView = (TextView) findViewById(R.id.filesEmptyView);
        fileViewPager = (ViewPager) findViewById(R.id.fileViewPager);
        fileViewPager.setOffscreenPageLimit(1);

        FileResource.list(this, id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray files = response.optJSONArray("files");
                filePagerAdapter = new FilePagerAdapter(DocumentActivity.this, files);
                fileViewPager.setAdapter(filePagerAdapter);

                progressBar.setVisibility(View.GONE);
                if (files.length() == 0) filesEmptyView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAllFailure(int statusCode, Header[] headers, byte[] responseBytes, Throwable throwable) {
                filesEmptyView.setText(R.string.error_loading_files);
                progressBar.setVisibility(View.GONE);
                filesEmptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.document_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download_file:
                downloadCurrentFile();
                return true;

            case R.id.download_document:
                downloadZip();
                return true;

            case R.id.share:
                DialogFragment dialog = DocShareFragment.newInstance(document.optString("id"));
                dialog.show(getSupportFragmentManager(), "DocShareFragment");
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Download the current displayed file.
     */
    private void downloadCurrentFile() {
        if (fileViewPager == null || filePagerAdapter == null) return;

        JSONObject file = filePagerAdapter.getObjectAt(fileViewPager.getCurrentItem());
        if (file == null) return;

        // Build the destination filename
        String mimeType = file.optString("mimetype");
        int position = fileViewPager.getCurrentItem();
        if (mimeType == null || !mimeType.contains("/")) return;
        String ext = mimeType.split("/")[1];
        String fileName = getTitle() + "-" + position + "." + ext;

        // Download the file
        String fileUrl = PreferenceUtil.getServerUrl(this) + "/api/file/" + file.optString("id") + "/data";
        downloadFile(fileUrl, fileName, getTitle().toString(), getString(R.string.downloading_file, position + 1));
    }

    /**
     * Download the document (all files zipped).
     */
    private void downloadZip() {
        if (document == null) return;
        String url = PreferenceUtil.getServerUrl(this) + "/api/file/zip?id=" + document.optString("id");
        String fileName = getTitle() + ".zip";
        downloadFile(url, fileName, getTitle().toString(), getString(R.string.downloading_document));
    }

    /**
     * Download a file using Android download manager.
     *
     * @param url URL to download
     * @param fileName Destination file name
     * @param title Notification title
     * @param description Notification description
     */
    private void downloadFile(String url, String fileName, String title, String description) {
        String authToken = PreferenceUtil.getAuthToken(this);
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.addRequestHeader("Cookie", "auth_token=" + authToken);
        request.setTitle(title);
        request.setDescription(description);
        downloadManager.enqueue(request);
    }

    public void onEvent(DocumentFullscreenEvent event) {
        findViewById(R.id.detailLayout).setVisibility(event.isFullscreen() ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}