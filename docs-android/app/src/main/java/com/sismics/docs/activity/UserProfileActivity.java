package com.sismics.docs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sismics.docs.R;
import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.model.application.ApplicationContext;
import com.sismics.docs.resource.UserResource;

import org.json.JSONObject;

/**
 * User profile activity.
 *
 * @author bgamard.
 */
public class UserProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        // Input username
        final String username = getIntent().getStringExtra("username");
        if (username == null) {
            finish();
            return;
        }

        // Setup the activity
        setTitle(username);
        setContentView(R.layout.userprofile_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Get the user and populate the view
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final View layoutView = findViewById(R.id.layout);
        progressBar.setVisibility(View.VISIBLE);
        layoutView.setVisibility(View.GONE);
        UserResource.get(this, username, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                TextView emailTextView = (TextView) findViewById(R.id.emailTextView);
                emailTextView.setText(json.optString("email"));

                TextView quotaTextView = (TextView) findViewById(R.id.quotaTextView);
                quotaTextView.setText(getString(R.string.storage_display,
                        Math.round(json.optLong("storage_current") / 1000000),
                        Math.round(json.optLong("storage_quota") / 1000000)));
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                layoutView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
