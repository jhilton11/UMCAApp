package com.appify.umcaapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.appify.umcaapp.model.Audio;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.downloader.request.DownloadRequestBuilder;

public class DownloadManagerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        Bundle b = intent.getExtras();
        int downloadId = b.getInt(AudioMsgActivity.DOWNLOAD_ID);
        String intentAction = b.getString("message");
        Log.d("DownloadReceiver", "Already in the download receiver");

        if (intentAction.equals(AudioMsgActivity.CANCEL_DOWNLOAD)) {
            PRDownloader.cancel(downloadId);
            Log.d("DownloadReceiver", "Cancelling download "+downloadId);
        } else if (intentAction.equals(AudioMsgActivity.OPEN_DOWNLOAD)) {
            Log.d("DownloadReceiver", "Cancelling download "+downloadId);
        } else if (intentAction.equals(AudioMsgActivity.DISMISS_NOTIFICATION)) {
            PRDownloader.cancel(downloadId);
            Log.d("DownloadReceiver", "Cancelling download "+downloadId);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.cancel(12345);
        }
    }
}
