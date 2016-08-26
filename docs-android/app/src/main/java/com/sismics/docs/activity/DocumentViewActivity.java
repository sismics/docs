package com.sismics.docs.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sismics.docs.R;
import com.sismics.docs.adapter.AclListAdapter;
import com.sismics.docs.adapter.CommentListAdapter;
import com.sismics.docs.adapter.FilePagerAdapter;
import com.sismics.docs.event.CommentAddEvent;
import com.sismics.docs.event.CommentDeleteEvent;
import com.sismics.docs.event.DocumentDeleteEvent;
import com.sismics.docs.event.DocumentEditEvent;
import com.sismics.docs.event.DocumentFullscreenEvent;
import com.sismics.docs.event.FileAddEvent;
import com.sismics.docs.event.FileDeleteEvent;
import com.sismics.docs.fragment.DocExportPdfFragment;
import com.sismics.docs.fragment.DocShareFragment;
import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.model.application.ApplicationContext;
import com.sismics.docs.resource.CommentResource;
import com.sismics.docs.resource.DocumentResource;
import com.sismics.docs.resource.FileResource;
import com.sismics.docs.service.FileUploadService;
import com.sismics.docs.util.NetworkUtil;
import com.sismics.docs.util.PreferenceUtil;
import com.sismics.docs.util.SpannableUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Document activity.
 * 
 * @author bgamard
 */
public class DocumentViewActivity extends AppCompatActivity {
    /**
     * Request code of adding file.
     */
    public static final int REQUEST_CODE_ADD_FILE = 1;

    /**
     * File view pager.
     */
    private ViewPager fileViewPager;

    /**
     * File pager adapter.
     */
    private FilePagerAdapter filePagerAdapter;

    /**
     * Comment list adapter.
     */
    private CommentListAdapter commentListAdapter;

    /**
     * Document displayed.
     */
    private JSONObject document;

    /**
     * Menu.
     */
    private Menu menu;

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
        setContentView(R.layout.document_view_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Fill the view
        refreshDocument(document);

        EventBus.getDefault().register(this);
    }

    /**
     * Refresh the displayed document.
     *
     * @param document Document in JSON format
     */
    private void refreshDocument(final JSONObject document) {
        this.document = document;

        String title = document.optString("title");
        String date = DateFormat.getDateFormat(this).format(new Date(document.optLong("create_date")));
        String description = document.optString("description");
        boolean shared = document.optBoolean("shared");
        String language = document.optString("language");
        JSONArray tags = document.optJSONArray("tags");

        // Setup the title
        setTitle(title);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        TextView titleTextView = (TextView) toolbar.getChildAt(1);
        if (titleTextView != null) {
            titleTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            titleTextView.setMarqueeRepeatLimit(-1);
            titleTextView.setFocusable(true);
            titleTextView.setFocusableInTouchMode(true);
        }

        // Fill the layout
        // Create date
        TextView createdDateTextView = (TextView) findViewById(R.id.createdDateTextView);
        createdDateTextView.setText(date);

        // Description
        TextView descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        if (description.isEmpty() || document.isNull("description")) {
            descriptionTextView.setVisibility(View.GONE);
        } else {
            descriptionTextView.setVisibility(View.VISIBLE);
            descriptionTextView.setText(description);
        }

        // Tags
        TextView tagTextView = (TextView) findViewById(R.id.tagTextView);
        if (tags.length() == 0) {
            tagTextView.setVisibility(View.GONE);
        } else {
            tagTextView.setVisibility(View.VISIBLE);
            tagTextView.setText(SpannableUtil.buildSpannableTags(tags));
        }

        // Language
        ImageView languageImageView = (ImageView) findViewById(R.id.languageImageView);
        languageImageView.setImageResource(getResources().getIdentifier(language, "drawable", getPackageName()));

        // Shared status
        ImageView sharedImageView = (ImageView) findViewById(R.id.sharedImageView);
        sharedImageView.setVisibility(shared ? View.VISIBLE : View.GONE);

        // Action edit document
        Button button = (Button) findViewById(R.id.actionEditDocument);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DocumentViewActivity.this, DocumentEditActivity.class);
                intent.putExtra("document", DocumentViewActivity.this.document.toString());
                startActivity(intent);
            }
        });

        // Action upload file
        button = (Button) findViewById(R.id.actionUploadFile);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                        .setType("*/*")
                        .putExtra("android.intent.extra.ALLOW_MULTIPLE", true)
                        .addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, getText(R.string.upload_from)), REQUEST_CODE_ADD_FILE);
            }
        });

        // Action download document
        button = (Button) findViewById(R.id.actionDownload);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadZip();
            }
        });

        // Action delete document
        button = (Button) findViewById(R.id.actionDelete);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDocument();
            }
        });

        // Action export PDF
        button = (Button) findViewById(R.id.actionExportPdf);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = DocExportPdfFragment.newInstance(
                        document.optString("id"), document.optString("title"));
                dialog.show(getSupportFragmentManager(), "DocExportPdfFragment");
            }
        });

        // Action share
        button = (Button) findViewById(R.id.actionSharing);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = DocShareFragment.newInstance(document.optString("id"));
                dialog.show(getSupportFragmentManager(), "DocShareFragment");
            }
        });

        // Action audit log
        button = (Button) findViewById(R.id.actionAuditLog);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DocumentViewActivity.this, AuditLogActivity.class);
                intent.putExtra("documentId", document.optString("id"));
                startActivity(intent);
            }
        });

        // Button add a comment
        ImageButton imageButton = (ImageButton) findViewById(R.id.addCommentBtn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText commentEditText = (EditText) findViewById(R.id.commentEditText);
                if (commentEditText.getText().length() == 0) {
                    // No content for the new comment
                    return;
                }

                Toast.makeText(DocumentViewActivity.this, R.string.adding_comment, Toast.LENGTH_LONG).show();

                CommentResource.add(DocumentViewActivity.this,
                        DocumentViewActivity.this.document.optString("id"),
                        commentEditText.getText().toString(),
                        new HttpCallback() {
                    public void onSuccess(JSONObject response) {
                        EventBus.getDefault().post(new CommentAddEvent(response));
                        commentEditText.setText("");
                    }

                    @Override
                    public void onFailure(JSONObject json, Exception e) {
                        Toast.makeText(DocumentViewActivity.this, R.string.comment_add_failure, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Grab the comments
        updateComments();

        // Grab the attached files
        updateFiles();

        // Grab the full document (used for ACLs, remaining metadata and writable status)
        updateDocument();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.document_view_activity, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info:
                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawerLayout.isDrawerVisible(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
                return true;

            case R.id.comments:
                drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;

            case R.id.download_file:
                downloadCurrentFile();
                return true;

            case R.id.delete_file:
                deleteCurrentFile();
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
        String fileName = document.optString("title") + "-" + position + "." + ext;

        // Download the file
        String fileUrl = PreferenceUtil.getServerUrl(this) + "/api/file/" + file.optString("id") + "/data";
        NetworkUtil.downloadFile(this, fileUrl, fileName, document.optString("title"), getString(R.string.download_file_title));
    }

    private void deleteCurrentFile() {
        if (fileViewPager == null || filePagerAdapter == null) return;

        final JSONObject file = filePagerAdapter.getObjectAt(fileViewPager.getCurrentItem());
        if (file == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.delete_file_title)
                .setMessage(R.string.delete_file_message)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the confirmation dialog
                        dialog.dismiss();

                        // Show a progress dialog while deleting
                        final ProgressDialog progressDialog = ProgressDialog.show(DocumentViewActivity.this,
                                getString(R.string.please_wait),
                                getString(R.string.file_deleting_message), true, true);

                        // Actual delete server call
                        final String fileId = file.optString("id");
                        FileResource.delete(DocumentViewActivity.this, fileId, new HttpCallback() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                EventBus.getDefault().post(new FileDeleteEvent(fileId));
                            }

                            @Override
                            public void onFailure(JSONObject json, Exception e) {
                                Toast.makeText(DocumentViewActivity.this, R.string.file_delete_failure, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFinish() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    /**
     * Download the document (all files zipped).
     */
    private void downloadZip() {
        if (document == null) return;
        String url = PreferenceUtil.getServerUrl(this) + "/api/file/zip?id=" + document.optString("id");
        String fileName = document.optString("title") + ".zip";
        NetworkUtil.downloadFile(this, url, fileName, document.optString("title"), getString(R.string.download_document_title));
    }

    /**
     * Delete the current document.
     */
    private void deleteDocument() {
        if (document == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.delete_document_title)
                .setMessage(R.string.delete_document_message)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the confirmation dialog
                        dialog.dismiss();

                        // Show a progress dialog while deleting
                        final ProgressDialog progressDialog = ProgressDialog.show(DocumentViewActivity.this,
                                getString(R.string.please_wait),
                                getString(R.string.document_deleting_message), true, true);

                        // Actual delete server call
                        final String documentId = document.optString("id");
                        DocumentResource.delete(DocumentViewActivity.this, documentId, new HttpCallback() {
                            @Override
                            public void onSuccess(JSONObject response) {
                                EventBus.getDefault().post(new DocumentDeleteEvent(documentId));
                            }

                            @Override
                            public void onFailure(JSONObject json, Exception e) {
                                Toast.makeText(DocumentViewActivity.this, R.string.document_delete_failure, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFinish() {
                                progressDialog.dismiss();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

    }

    /**
     * A document fullscreen event has been fired.
     *
     * @param event Document fullscreen event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DocumentFullscreenEvent event) {
        findViewById(R.id.detailLayout).setVisibility(event.isFullscreen() ? View.GONE : View.VISIBLE);
    }

    /**
     * A document edit event has been fired.
     *
     * @param event Document edit event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DocumentEditEvent event) {
        if (document == null) return;
        if (event.getDocument().optString("id").equals(document.optString("id"))) {
            // The current document has been modified, refresh it
            refreshDocument(event.getDocument());
        }
    }

    /**
     * A document delete event has been fired.
     *
     * @param event Document delete event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DocumentDeleteEvent event) {
        if (document == null) return;
        if (event.getDocumentId().equals(document.optString("id"))) {
            // The current document has been deleted, close this activity
            finish();
        }
    }

    /**
     * A file delete event has been fired.
     *
     * @param event File delete event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FileDeleteEvent event) {
        if (filePagerAdapter == null) return;
        filePagerAdapter.remove(event.getFileId());
        final TextView filesEmptyView = (TextView) findViewById(R.id.filesEmptyView);
        if (filePagerAdapter.getCount() == 0) filesEmptyView.setVisibility(View.VISIBLE);
    }

    /**
     * A file add event has been fired.
     *
     * @param event File add event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FileAddEvent event) {
        if (document == null) return;
        if (document.optString("id").equals(event.getDocumentId())) {
            updateFiles();
        }
    }

    /**
     * A comment add event has been fired.
     *
     * @param event Comment add event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommentAddEvent event) {
        if (commentListAdapter == null) return;
        TextView emptyView = (TextView) findViewById(R.id.commentEmptyView);
        ListView listView = (ListView) findViewById(R.id.commentListView);
        emptyView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        commentListAdapter.add(event.getComment());
    }

    /**
     * A comment delete event has been fired.
     *
     * @param event Comment add event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CommentDeleteEvent event) {
        if (commentListAdapter == null) return;
        TextView emptyView = (TextView) findViewById(R.id.commentEmptyView);
        ListView listView = (ListView) findViewById(R.id.commentListView);
        commentListAdapter.remove(event.getCommentId());
        if (commentListAdapter.getCount() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (document == null) return;

        if (requestCode == REQUEST_CODE_ADD_FILE && resultCode == RESULT_OK) {
            List<Uri> uriList = new ArrayList<>();
            // Single file upload
            if (data.getData() != null) {
                uriList.add(data.getData());
            }

            // Handle multiple file upload
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); ++i) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        if (uri != null) {
                            uriList.add(uri);
                        }
                    }
                }
            }

            // Upload all files
            for (Uri uri : uriList) {
                Intent intent = new Intent(this, FileUploadService.class)
                        .putExtra(FileUploadService.PARAM_URI, uri)
                        .putExtra(FileUploadService.PARAM_DOCUMENT_ID, document.optString("id"));
                startService(intent);
            }
        }
    }

    /**
     * Update the document model.
     */
    private void updateDocument() {
        if (document == null) return;

        // Silently get the document to know if it is writable by the current user
        // If this call fails or is slow and the document is read-only,
        // write actions will be allowed and will fail
        DocumentResource.get(this, document.optString("id"), new HttpCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                document = response;
                boolean writable = document.optBoolean("writable");

                if (menu != null) {
                    menu.findItem(R.id.delete_file).setVisible(writable);
                }

                // Action only available if the document is writable
                findViewById(R.id.actionEditDocument).setVisibility(writable ? View.VISIBLE : View.GONE);
                findViewById(R.id.actionUploadFile).setVisibility(writable ? View.VISIBLE : View.GONE);
                findViewById(R.id.actionSharing).setVisibility(writable ? View.VISIBLE : View.GONE);
                findViewById(R.id.actionDelete).setVisibility(writable ? View.VISIBLE : View.GONE);

                // ACLs
                ListView aclListView = (ListView) findViewById(R.id.aclListView);
                final AclListAdapter aclListAdapter = new AclListAdapter(document.optJSONArray("acls"));
                aclListView.setAdapter(aclListAdapter);
                aclListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        AclListAdapter.AclItem acl = aclListAdapter.getItem(position);
                        if (acl.getType().equals("USER")) {
                            Intent intent = new Intent(DocumentViewActivity.this, UserProfileActivity.class);
                            intent.putExtra("username", acl.getName());
                            startActivity(intent);
                        } else if (acl.getType().equals("GROUP")) {
                            Intent intent = new Intent(DocumentViewActivity.this, GroupProfileActivity.class);
                            intent.putExtra("name", acl.getName());
                            startActivity(intent);
                        }
                    }
                });

                // Remaining metadata
                TextView creatorTextView = (TextView) findViewById(R.id.creatorTextView);
                final String creator = document.optString("creator");
                creatorTextView.setText(creator);
                creatorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(DocumentViewActivity.this, UserProfileActivity.class);
                        intent.putExtra("username", creator);
                        startActivity(intent);
                    }
                });

                // Contributors
                TextView contributorsTextView = (TextView) findViewById(R.id.contributorsTextView);
                contributorsTextView.setText(SpannableUtil.buildSpannableContributors(document.optJSONArray("contributors")));

                // Relations
                JSONArray relations = document.optJSONArray("relations");
                if (relations.length() > 0) {
                    TextView relationsTextView = (TextView) findViewById(R.id.relationsTextView);
                    relationsTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    relationsTextView.setText(SpannableUtil.buildSpannableRelations(relations));
                } else {
                    findViewById(R.id.relationsLayout).setVisibility(View.GONE);
                }

                // Additional dublincore metadata
                displayDublincoreMetadata(R.id.subjectTextView, R.id.subjectLayout, "subject");
                displayDublincoreMetadata(R.id.identifierTextView, R.id.identifierLayout, "identifier");
                displayDublincoreMetadata(R.id.publisherTextView, R.id.publisherLayout, "publisher");
                displayDublincoreMetadata(R.id.formatTextView, R.id.formatLayout, "format");
                displayDublincoreMetadata(R.id.sourceTextView, R.id.sourceLayout, "source");
                displayDublincoreMetadata(R.id.typeTextView, R.id.typeLayout, "type");
                displayDublincoreMetadata(R.id.coverageTextView, R.id.coverageLayout, "coverage");
                displayDublincoreMetadata(R.id.rightsTextView, R.id.rightsLayout, "rights");
            }
        });
    }

    /**
     * Display a dublincore metadata.
     *
     * @param textViewId TextView ID
     * @param blockViewId View ID
     * @param name Name
     */
    private void displayDublincoreMetadata(int textViewId, int blockViewId, String name) {
        if (document == null) return;
        String value = document.optString(name);
        if (document.isNull(name) || value.isEmpty()) {
            findViewById(blockViewId).setVisibility(View.GONE);
            return;
        }

        findViewById(blockViewId).setVisibility(View.VISIBLE);
        TextView textView = (TextView) findViewById(textViewId);
        textView.setText(value);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        switch (view.getId()) {
            case R.id.commentListView:
                if (commentListAdapter == null || document == null) return;
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                JSONObject comment = commentListAdapter.getItem(info.position);
                boolean writable = document.optBoolean("writable");
                String creator = comment.optString("creator");
                String username = ApplicationContext.getInstance().getUserInfo().optString("username");
                if (writable || creator.equals(username)) {
                    menu.add(Menu.NONE, 0, 0, getString(R.string.comment_delete));
                }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Use real ids if more than one item someday
        if (item.getItemId() == 0) {
            // Delete a comment
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
            if (commentListAdapter == null) return false;
            JSONObject comment = commentListAdapter.getItem(info.position);
            final String commentId = comment.optString("id");
            Toast.makeText(DocumentViewActivity.this, R.string.deleting_comment, Toast.LENGTH_LONG).show();

            CommentResource.remove(DocumentViewActivity.this, commentId, new HttpCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    EventBus.getDefault().post(new CommentDeleteEvent(commentId));
                }

                @Override
                public void onFailure(JSONObject json, Exception e) {
                    Toast.makeText(DocumentViewActivity.this, R.string.error_deleting_comment, Toast.LENGTH_LONG).show();
                }
            });

            return true;
        }

        return false;
    }

    /**
     * Refresh comments list.
     */
    private void updateComments() {
        if (document == null) return;

        final View progressBar = findViewById(R.id.commentProgressView);
        final TextView emptyView = (TextView) findViewById(R.id.commentEmptyView);
        final ListView listView = (ListView) findViewById(R.id.commentListView);
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        registerForContextMenu(listView);

        CommentResource.list(this, document.optString("id"), new HttpCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                JSONArray comments = response.optJSONArray("comments");
                commentListAdapter = new CommentListAdapter(DocumentViewActivity.this, comments);
                listView.setAdapter(commentListAdapter);
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                if (comments.length() == 0) {
                    listView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(JSONObject json, Exception e) {
                emptyView.setText(R.string.error_loading_comments);
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Refresh files list.
     */
    private void updateFiles() {
        if (document == null) return;

        final View progressBar = findViewById(R.id.progressBar);
        final TextView filesEmptyView = (TextView) findViewById(R.id.filesEmptyView);
        fileViewPager = (ViewPager) findViewById(R.id.fileViewPager);
        fileViewPager.setOffscreenPageLimit(1);
        fileViewPager.setAdapter(null);
        progressBar.setVisibility(View.VISIBLE);
        filesEmptyView.setVisibility(View.GONE);

        FileResource.list(this, document.optString("id"), new HttpCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                JSONArray files = response.optJSONArray("files");
                filePagerAdapter = new FilePagerAdapter(DocumentViewActivity.this, files);
                fileViewPager.setAdapter(filePagerAdapter);

                progressBar.setVisibility(View.GONE);
                if (files.length() == 0) filesEmptyView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(JSONObject json, Exception e) {
                filesEmptyView.setText(R.string.error_loading_files);
                progressBar.setVisibility(View.GONE);
                filesEmptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}