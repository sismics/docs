package com.sismics.docs.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sismics.docs.R;
import com.sismics.docs.util.NetworkUtil;
import com.sismics.docs.util.PreferenceUtil;

import java.util.Locale;

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
     * @param title Document title
     */
    public static DocExportPdfFragment newInstance(String id, String title) {
        DocExportPdfFragment fragment = new DocExportPdfFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("title", title);
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
        final SeekBar marginSeekBar = (SeekBar) view.findViewById(R.id.marginSeekBar);
        final CheckBox exportMetadataCheckbox = (CheckBox) view.findViewById(R.id.exportMetadataCheckbox);
        final CheckBox exportCommentsCheckbox = (CheckBox) view.findViewById(R.id.exportCommentsCheckbox);
        final CheckBox fitToPageCheckbox = (CheckBox) view.findViewById(R.id.fitToPageCheckbox);
        final TextView marginValueText = (TextView) view.findViewById(R.id.marginValueText);

        // Margin label follow seekbar value
        marginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                marginValueText.setText(String.format(Locale.ENGLISH, "%d", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Build the dialog
        builder.setView(view)
                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Download the PDF
                        String pdfUrl = PreferenceUtil.getServerUrl(getActivity()) + "/api/document/" + getArguments().getString("id") + "/pdf?" +
                                "metadata=" + exportMetadataCheckbox.isChecked() + "&comments=" + exportCommentsCheckbox.isChecked() + "&fitimagetopage=" + fitToPageCheckbox.isChecked() +
                                "&margin=" + marginSeekBar.getProgress();
                        String title = getArguments().getString("title");
                        NetworkUtil.downloadFile(getActivity(), pdfUrl, title + ".pdf", title, getString(R.string.download_pdf_title));

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
