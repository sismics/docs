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

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Group profile activity.
 *
 * @author bgamard.
 */
public class GroupProfileActivity extends AppCompatActivity {
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

        // Input name
        final String name = getIntent().getStringExtra("name");
        if (name == null) {
            finish();
            return;
        }

        // Setup the activity
        setTitle(name);
        setContentView(R.layout.groupprofile_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Get the group and populate the view
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final View layoutView = findViewById(R.id.layout);
        progressBar.setVisibility(View.VISIBLE);
        layoutView.setVisibility(View.GONE);
        UserResource.get(this, name, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                TextView membersTextView = (TextView) findViewById(R.id.membersTextView);
                JSONArray members = json.optJSONArray("members");
                String output = "";
                for (int i = 0; i < members.length(); i++) {
                    output += members.optString(i) + "; ";
                }
                membersTextView.setText(output);
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
