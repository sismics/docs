package com.sismics.docs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sismics.docs.R;
import com.sismics.docs.adapter.AuditLogListAdapter;
import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.model.application.ApplicationContext;
import com.sismics.docs.resource.AuditLogResource;

import org.json.JSONObject;

/**
 * Audit log activity.
 *
 * @author bgamard.
 */
public class AuditLogActivity extends AppCompatActivity {
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

        // Input document ID (optional)
        final String documentId = getIntent().getStringExtra("documentId");

        // Setup the activity
        setContentView(R.layout.auditlog_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Configure the swipe refresh layout
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshView(documentId);
            }
        });

        // Navigate to user profile on click
        final ListView auditLogListView = findViewById(R.id.auditLogListView);
        auditLogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (auditLogListView.getAdapter() == null) {
                    return;
                }
                AuditLogListAdapter adapter = (AuditLogListAdapter) auditLogListView.getAdapter();
                String username = adapter.getItem(position).optString("username");
                Intent intent = new Intent(AuditLogActivity.this, UserProfileActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        // Get audit log list
        refreshView(documentId);
    }

    /**
     * Refresh the view.
     */
    private void refreshView(String documentId) {
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        final ListView auditLogListView = findViewById(R.id.auditLogListView);
        progressBar.setVisibility(View.VISIBLE);
        auditLogListView.setVisibility(View.GONE);
        AuditLogResource.list(this, documentId, new HttpCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                auditLogListView.setAdapter(new AuditLogListAdapter(AuditLogActivity.this, response.optJSONArray("logs")));
            }

            @Override
            public void onFinish() {
                progressBar.setVisibility(View.GONE);
                auditLogListView.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
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
