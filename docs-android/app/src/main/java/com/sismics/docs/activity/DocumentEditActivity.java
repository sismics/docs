package com.sismics.docs.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sismics.docs.R;
import com.sismics.docs.adapter.TagAutoCompleteAdapter;
import com.sismics.docs.ui.view.TagsCompleteTextView;
import com.sismics.docs.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Document edition activity.
 *
 * @author bgamard.
 */
public class DocumentEditActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle args) {
        super.onCreate(args);

        // Handle activity context
        if (getIntent() == null) {
            finish();
            return;
        }

        // Setup the activity
        setContentView(R.layout.document_edit_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Spinner languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
        languageSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[] { "French", "English", "Japanese" }));

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

        TagsCompleteTextView tagsEditText = (TagsCompleteTextView) findViewById(R.id.tagsEditText);
        tagsEditText.allowDuplicates(false);
        tagsEditText.setAdapter(new TagAutoCompleteAdapter(this, 0, tagList));
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
