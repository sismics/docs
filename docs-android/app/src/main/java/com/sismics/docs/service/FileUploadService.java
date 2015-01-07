package com.sismics.docs.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.sismics.docs.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service to upload a file to a document in the background.
 *
 * @author bgamard
 */
public class FileUploadService extends IntentService {
    private static final String TAG = "FileUploadService";

    private static final int UPLOAD_NOTIFICATION_ID = 1;
    private static final int UPLOAD_NOTIFICATION_ID_DONE = 2;
    public static final String PARAM_URI = "uri";

    private NotificationManager notificationManager;
    private Builder notification;
    private PowerManager.WakeLock wakeLock;

    public FileUploadService() {
        super(FileUploadService.class.getName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification = new NotificationCompat.Builder(this);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        wakeLock.acquire();
        try {
            onStart();
            handleFileUpload((Uri) intent.getParcelableExtra(PARAM_URI));
        } catch (Exception e) {
            Log.e(TAG, "Error uploading the file", e);
            onError();
        } finally {
            wakeLock.release();
        }
    }

    /**
     * Actually uploading the file.
     *
     * @param uri Data URI
     * @throws IOException
     */
    private void handleFileUpload(final Uri uri) throws IOException {
        InputStream is = null;
        try {
            is = getContentResolver().openInputStream(uri);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }

        onComplete();
    }

    /**
     * On upload start.
     */
    private void onStart() {
        notification.setContentTitle(getString(R.string.upload_notification_title))
                .setContentText(getString(R.string.upload_notification_message))
                .setContentIntent(PendingIntent.getBroadcast(this, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.ic_launcher)
                .setProgress(100, 0, true)
                .setOngoing(true);

        startForeground(UPLOAD_NOTIFICATION_ID, notification.build());
    }

    /**
     * On upload progress.
     *
     * @param progress Progression (100 based)
     */
    private void onProgress(final int progress) {
        notification.setContentTitle(getString(R.string.upload_notification_title))
                .setContentText(getString(R.string.upload_notification_message))
                .setSmallIcon(R.drawable.ic_launcher)
                .setProgress(100, progress, false)
                .setOngoing(true);

        startForeground(UPLOAD_NOTIFICATION_ID, notification.build());
    }

    /**
     * On upload complete.
     */
    private void onComplete() {
        stopForeground(true);
    }

    /**
     * On upload error.
     */
    private void onError() {
        stopForeground(false);

        notification.setContentTitle(getString(R.string.upload_notification_title))
                .setContentText(getString(R.string.upload_notification_error))
                .setSmallIcon(R.drawable.ic_launcher)
                .setProgress(0, 0, false)
                .setOngoing(false);

        notificationManager.notify(UPLOAD_NOTIFICATION_ID_DONE, notification.build());
    }
}