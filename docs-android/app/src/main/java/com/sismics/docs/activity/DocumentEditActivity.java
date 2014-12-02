package com.sismics.docs.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;

import com.sismics.docs.R;

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

        MultiAutoCompleteTextView tagsEditText = (MultiAutoCompleteTextView) findViewById(R.id.tagsEditText);
        tagsEditText.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Caluire", "Appartement", "Banque", "Assurance"}));
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
