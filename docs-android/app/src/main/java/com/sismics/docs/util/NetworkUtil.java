package com.sismics.docs.util;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Utility class for network actions.
 *
 * @author bgamard.
 */
public class NetworkUtil {
    /**
     * Download a file using Android download manager.
     *
     * @param url URL to download
     * @param fileName Destination file name
     * @param title Notification title
     * @param description Notification description
     */
    public static void downloadFile(Activity activity, String url, String fileName, String title, String description) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
            return;
        }

        String authToken = PreferenceUtil.getAuthToken(activity);
        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.addRequestHeader("Cookie", "auth_token=" + authToken);
        request.setTitle(title);
        request.setDescription(description);
        downloadManager.enqueue(request);
    }
}
