package com.sismics.docs.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

import com.sismics.docs.R;
import com.sismics.docs.adapter.LanguageAdapter;
import com.sismics.docs.adapter.TagAutoCompleteAdapter;
import com.sismics.docs.event.SearchEvent;
import com.sismics.docs.ui.view.TagsCompleteTextView;
import com.sismics.docs.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

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
        Spinner languageSpinner = (Spinner) view.findViewById(R.id.languageSpinner);
        TagsCompleteTextView tagsEditText = (TagsCompleteTextView) view.findViewById(R.id.tagsEditText);

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
        JSONArray tagArray = tags.optJSONArray("stats");

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
                        EventBus.getDefault().post(new SearchEvent(null));
                        getDialog().cancel();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
