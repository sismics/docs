package com.sismics.docs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.sismics.docs.R;
import com.sismics.docs.adapter.FilePagerAdapter;
import com.sismics.docs.model.application.ApplicationContext;
import com.sismics.docs.resource.FileResource;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Document activity.
 * 
 * @author bgamard
 */
public class DocumentActivity extends ActionBarActivity {
    
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

        JSONObject document;
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

        // Fill the layout
        setTitle(title);
        TextView createdDateTextView = (TextView) findViewById(R.id.createdDateTextView);
        createdDateTextView.setText(date);
        TextView languageTextView = (TextView) findViewById(R.id.languageTextView);
        languageTextView.setText(document.optString("language"));
        TextView descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        if (description == null || description.isEmpty()) {
            descriptionTextView.setVisibility(View.GONE);
        } else {
            descriptionTextView.setText(description);
        }

        // Grab the attached files
        FileResource.list(this, id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ViewPager fileViewPager = (ViewPager) findViewById(R.id.fileViewPager);
                fileViewPager.setOffscreenPageLimit(1);
                fileViewPager.setAdapter(new FilePagerAdapter(DocumentActivity.this, response.optJSONArray("files")));
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}