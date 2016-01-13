package com.sismics.docs.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.sismics.docs.R;

/**
 * Export PDF dialog fragment.
 *
 * @author bgamard.
 */
public class DocExportPdfFragment extends DialogFragment {
    /**
     * Export PDF dialog fragment.
     *
     * @param id Document ID
     */
    public static DocExportPdfFragment newInstance(String id) {
        DocExportPdfFragment fragment = new DocExportPdfFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Setup the view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.document_export_pdf_dialog, null);

        // Build the dialog
        builder.setView(view)
                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
