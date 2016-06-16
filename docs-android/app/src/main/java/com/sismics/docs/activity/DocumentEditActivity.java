package com.sismics.docs.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.sismics.docs.R;
import com.sismics.docs.adapter.LanguageAdapter;
import com.sismics.docs.adapter.TagAutoCompleteAdapter;
import com.sismics.docs.event.DocumentAddEvent;
import com.sismics.docs.event.DocumentEditEvent;
import com.sismics.docs.listener.HttpCallback;
import com.sismics.docs.resource.DocumentResource;
import com.sismics.docs.ui.form.Validator;
import com.sismics.docs.ui.form.validator.Required;
import com.sismics.docs.ui.view.DatePickerView;
import com.sismics.docs.ui.view.TagsCompleteTextView;
import com.sismics.docs.util.PreferenceUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Document edition activity.
 *
 * @author bgamard.
 */
public class DocumentEditActivity extends AppCompatActivity {
    /**
     * Document edited.
     */
    private JSONObject document;

    /**
     * Form validator.
     */
    private Validator validator;

    // View cache
    private EditText titleEditText;
    private EditText descriptionEditText;
    private TagsCompleteTextView tagsEditText;
    private Spinner languageSpinner;
    private DatePickerView datePickerView;

    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);

        // Handle activity context
        if (getIntent() == null) {
            finish();
            return;
        }

        // Parse input document
        String documentJson = getIntent().getStringExtra("document");
        if (documentJson != null) {
            try {
                document = new JSONObject(documentJson);
            } catch (JSONException e) {
                finish();
                return;
            }
        }

        // Setup the activity
        setContentView(R.layout.document_edit_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
        tagsEditText = (TagsCompleteTextView) findViewById(R.id.tagsEditText);
        datePickerView = (DatePickerView) findViewById(R.id.dateEditText);
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);

        // Language spinner
        LanguageAdapter languageAdapter = new LanguageAdapter(this, false);
        languageSpinner.setAdapter(languageAdapter);

        // Tags auto-complete
        JSONObject tags = PreferenceUtil.getCachedJson(this, PreferenceUtil.PREF_CACHED_TAGS_JSON);
        if (tags == null) {
            finish();
            return;
        }
        JSONArray tagArray = tags.optJSONArray("tags");

        List<JSONObject> tagList = new ArrayList<>();
        for (int i = 0; i < tagArray.length(); i++) {
            tagList.add(tagArray.optJSONObject(i));
        }

        tagsEditText.allowDuplicates(false);
        tagsEditText.setAdapter(new TagAutoCompleteAdapter(this, 0, tagList));

        // Validation
        validator = new Validator(this, true);
        validator.addValidable(titleEditText, new Required());

        // Fill the activity
        if (document == null) {
            datePickerView.setDate(new Date());
        } else {
            setTitle(R.string.edit_document);
            titleEditText.setText(document.optString("title"));
            descriptionEditText.setText(document.isNull("description") ? "" : document.optString("description"));
            datePickerView.setDate(new Date(document.optLong("create_date")));
            languageSpinner.setSelection(languageAdapter.getItemPosition(document.optString("language")));
            JSONArray documentTags = document.optJSONArray("tags");
            for (int i = 0; i < documentTags.length(); i++) {
                tagsEditText.addObject(documentTags.optJSONObject(i));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.document_edit_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                validator.validate();
                if (!validator.isValidated()) {
                    return true;
                }

                // Metadata
                final String title = titleEditText.getText().toString();
                final String description = descriptionEditText.getText().toString();
                LanguageAdapter.Language language = (LanguageAdapter.Language) languageSpinner.getSelectedItem();
                final String langId = language.getId();
                final long createDate = datePickerView.getDate().getTime();
                Set<String> tagIdList = new HashSet<>();
                for (Object object : tagsEditText.getObjects()) {
                    JSONObject tag = (JSONObject) object;
                    tagIdList.add(tag.optString("id"));
                }

                // Cancellable progress dialog
                final ProgressDialog progressDialog = ProgressDialog.show(this,
                        getString(R.string.please_wait),
                        getString(R.string.document_editing_message), true, true);

                // Server callback
                HttpCallback callback = new HttpCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        // Build a fake document JSON to update the UI
                        final JSONObject outputDoc = new JSONObject();
                        try {
                            if (document == null) {
                                outputDoc.putOpt("id", response.optString("id"));
                                outputDoc.putOpt("shared", false);
                            } else {
                                outputDoc.putOpt("id", document.optString("id"));
                                outputDoc.putOpt("shared", document.optBoolean("shared"));
                            }
                            outputDoc.putOpt("title", title);
                            outputDoc.putOpt("description", description);
                            outputDoc.putOpt("language", langId);
                            outputDoc.putOpt("create_date", createDate);
                            JSONArray tags = new JSONArray();
                            for (Object object : tagsEditText.getObjects()) {
                                tags.put(object);
                            }
                            outputDoc.putOpt("tags", tags);
                        } catch (JSONException e) {
                            Log.e(DocumentEditActivity.class.getSimpleName(), "Error building JSON for document", e);
                        }

                        // Fire the right event
                        if (document == null) {
                            EventBus.getDefault().post(new DocumentAddEvent(outputDoc));
                        } else {
                            EventBus.getDefault().post(new DocumentEditEvent(outputDoc));
                        }
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(JSONObject json, Exception e) {
                        Toast.makeText(DocumentEditActivity.this, R.string.error_editing_document, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFinish() {
                        progressDialog.dismiss();
                    }
                };

                // Actual server call
                if (document == null) {
                    DocumentResource.add(this, title, description, tagIdList, langId, createDate, callback);
                } else {
                    DocumentResource.edit(this, document.optString("id"), title, description, tagIdList, langId, createDate, callback);
                }
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
