package com.sismics.docs.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.sismics.docs.R;

/**
 * Utility class for dialogs.
 * 
 * @author bgamard
 */
public class DialogUtil {
    /**
     * Create a dialog with an OK button.
     *
     * @param activity Context activity
     * @param title Dialog title
     * @param message Dialog message
     */
    public static void showOkDialog(Activity activity, int title, int message) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        
        builder.setTitle(title)
        .setMessage(message)
        .setCancelable(true)
        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        }).create().show();
    }
}
