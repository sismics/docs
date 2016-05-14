package com.sismics.docs.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.sismics.docs.R;
import com.sismics.docs.adapter.LanguageAdapter;
import com.sismics.docs.adapter.TagAutoCompleteAdapter;
import com.sismics.docs.event.AdvancedSearchEvent;
import com.sismics.docs.ui.view.DatePickerView;
import com.sismics.docs.ui.view.TagsCompleteTextView;
import com.sismics.docs.util.PreferenceUtil;
import com.sismics.docs.util.SearchQueryBuilder;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Advanced search fragment.
 *
 * @author bgamard.
 */
public class SearchFragment extends DialogFragment {
    /**
     * Document sharing dialog fragment
     */
    public static SearchFragment newInstance() {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Setup the view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.search_dialog, null);
        final EditText searchEditText = (EditText) view.findViewById(R.id.searchEditText);
        final EditText fulltextEditText = (EditText) view.findViewById(R.id.fulltextEditText);
        final EditText creatorEditText = (EditText) view.findViewById(R.id.creatorEditText);
        final CheckBox sharedCheckbox = (CheckBox) view.findViewById(R.id.sharedCheckbox);
        final Spinner languageSpinner = (Spinner) view.findViewById(R.id.languageSpinner);
        final DatePickerView beforeDatePicker = (DatePickerView) view.findViewById(R.id.beforeDatePicker);
        final DatePickerView afterDatePicker = (DatePickerView) view.findViewById(R.id.afterDatePicker);
        final TagsCompleteTextView tagsEditText = (TagsCompleteTextView) view.findViewById(R.id.tagsEditText);

        // Language spinner
        LanguageAdapter languageAdapter = new LanguageAdapter(getActivity(), true);
        languageSpinner.setAdapter(languageAdapter);

        // Tags auto-complete
        JSONObject tags = PreferenceUtil.getCachedJson(getActivity(), PreferenceUtil.PREF_CACHED_TAGS_JSON);
        if (tags == null) {
            Dialog dialog = builder.create();
            dialog.cancel();
            return dialog;
        }
        JSONArray tagArray = tags.optJSONArray("tags");

        List<JSONObject> tagList = new ArrayList<>();
        for (int i = 0; i < tagArray.length(); i++) {
            tagList.add(tagArray.optJSONObject(i));
        }

        tagsEditText.allowDuplicates(false);
        tagsEditText.setAdapter(new TagAutoCompleteAdapter(getActivity(), 0, tagList));

        // Build the dialog
        builder.setView(view)
                .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Build the simple criterias
                        SearchQueryBuilder queryBuilder = new SearchQueryBuilder()
                                .simpleSearch(searchEditText.getText().toString())
                                .creator(creatorEditText.getText().toString())
                                .shared(sharedCheckbox.isChecked())
                                .language(((LanguageAdapter.Language) languageSpinner.getSelectedItem()).getId())
                                .before(beforeDatePicker.getDate())
                                .after(afterDatePicker.getDate());

                        // Fulltext criteria
                        String fulltextCriteria = fulltextEditText.getText().toString();
                        if (!fulltextCriteria.trim().isEmpty()) {
                            String[] criterias = fulltextCriteria.split(" ");
                            for (String criteria : criterias) {
                                queryBuilder.fulltextSearch(criteria);
                            }
                        }

                        // Tags criteria
                        for (Object object : tagsEditText.getObjects()) {
                            JSONObject tag = (JSONObject) object;
                            queryBuilder.tag(tag.optString("name"));
                        }

                        // Send the advanced search event
                        EventBus.getDefault().post(new AdvancedSearchEvent(queryBuilder.build()));

                        getDialog().cancel();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }
}
