package com.sismics.docs.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

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
    public static void downloadFile(Context context, String url, String fileName, String title, String description) {
        String authToken = PreferenceUtil.getAuthToken(context);
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.addRequestHeader("Cookie", "auth_token=" + authToken);
        request.setTitle(title);
        request.setDescription(description);
        downloadManager.enqueue(request);
    }
}
