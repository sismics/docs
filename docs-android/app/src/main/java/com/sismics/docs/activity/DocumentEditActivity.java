package com.sismics.docs.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;

import com.sismics.docs.R;
import com.sismics.docs.adapter.LanguageAdapter;
import com.sismics.docs.adapter.TagAutoCompleteAdapter;
import com.sismics.docs.event.DocumentEditEvent;
import com.sismics.docs.ui.view.DatePickerView;
import com.sismics.docs.ui.view.TagsCompleteTextView;
import com.sismics.docs.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Document edition activity.
 *
 * @author bgamard.
 */
public class DocumentEditActivity extends ActionBarActivity {
    /**
     * Document edited.
     */
    JSONObject document;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
        tagsEditText = (TagsCompleteTextView) findViewById(R.id.tagsEditText);
        datePickerView = (DatePickerView) findViewById(R.id.dateEditText);
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);

        // Language spinner
        LanguageAdapter languageAdapter = new LanguageAdapter(this);
        languageSpinner.setAdapter(languageAdapter);

        // Tags auto-complete
        JSONObject tags = PreferenceUtil.getCachedJson(this, PreferenceUtil.PREF_CACHED_TAGS_JSON);
        if (tags == null) {
            finish();
            return;
        }
        JSONArray tagArray = tags.optJSONArray("stats");

        List<JSONObject> tagList = new ArrayList<>();
        for (int i = 0; i < tagArray.length(); i++) {
            tagList.add(tagArray.optJSONObject(i));
        }

        tagsEditText.allowDuplicates(false);
        tagsEditText.setAdapter(new TagAutoCompleteAdapter(this, 0, tagList));

        // Fill the activity
        if (document == null) {
            datePickerView.setDate(new Date());
        } else {
            setTitle(R.string.edit_document);
            titleEditText.setText(document.optString("title"));
            descriptionEditText.setText(document.optString("description"));
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
                JSONObject outputDoc = new JSONObject();
                try {
                    if (document != null) {
                        outputDoc.putOpt("id", document.optString("id"));
                        outputDoc.putOpt("shared", document.optBoolean("shared"));
                    }
                    outputDoc.putOpt("title", titleEditText.getText().toString());
                    outputDoc.putOpt("description", descriptionEditText.getText().toString());
                    if (languageSpinner.getSelectedItem() != null) {
                        LanguageAdapter.Language language = (LanguageAdapter.Language) languageSpinner.getSelectedItem();
                        outputDoc.putOpt("language", language.getId());
                    }
                    if (datePickerView.getDate() != null) {
                        outputDoc.putOpt("create_date", datePickerView.getDate().getTime());
                    }
                    JSONArray tags = new JSONArray();
                    for (Object object : tagsEditText.getObjects()) {
                        if (object instanceof JSONObject) {
                            tags.put(object);
                        }
                    }
                    outputDoc.putOpt("tags", tags);
                } catch (JSONException e) {
                    Log.e(DocumentEditActivity.class.getSimpleName(), "Error building JSON for document", e);
                }

                EventBus.getDefault().post(new DocumentEditEvent(outputDoc));
                setResult(RESULT_OK);
                finish();
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
