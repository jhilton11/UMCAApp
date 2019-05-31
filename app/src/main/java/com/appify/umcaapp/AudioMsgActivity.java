package com.appify.umcaapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appify.umcaapp.model.Audio;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class AudioMsgActivity extends AppCompatActivity {
    //public static final String PAUSE_DOWNLOAD = "pause_download";
    //public static final String RESUME_DOWNLOAD = "resume_download";
    public static final String CANCEL_DOWNLOAD = "cancel_download";
    public static final String OPEN_DOWNLOAD = "open_download_folder";
    public static final String DISMISS_NOTIFICATION = "dismiss_notification";
    private TextView playedTv, totalTv, titleTv;
    private ImageView playButton, stopButton, downloadBtn;
    private SeekBar mSeekBar;
    private ProgressBar mProgressBar;

    private Audio audioMsg;
    private MyMusicService service;
    private Intent intent;
    private StateReceiver mReceiver;
    private NetworkReceiver mNetworkReceiver;
    private StorageReference storageRef;
    private Handler mHandler;
    private NotificationManagerCompat notificationManagerCompat;
    private NotificationCompat.Builder builder;
    private String msgUrl;
    private boolean mBound = false;
    private boolean isConnected = false;
    private int downloadID;
    private String totalTime = "";
    private String id;

    public static final String AUDIO_MESSAGE_KEY = "audio";
    public static final String DOWNLOAD_ID = "download_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_msg);

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            isConnected = true;
        }

        audioMsg = (Audio) getIntent().getSerializableExtra(AudioMsgActivity.AUDIO_MESSAGE_KEY);

        notificationManagerCompat = NotificationManagerCompat.from(this);
        builder = new NotificationCompat.Builder(this, "DOWNLOAD_CHANNEL");

        PRDownloader.initialize(this);

        msgUrl = audioMsg.getMsgUrl();

        playedTv = findViewById(R.id.played_tv);
        totalTv = findViewById(R.id.duration_tv);
        titleTv = findViewById(R.id.title_tv);

        playButton = findViewById(R.id.play);
        stopButton = findViewById(R.id.stop);
        downloadBtn = findViewById(R.id.download_btn);

        mSeekBar = findViewById(R.id.seekBar);
        mSeekBar.setMax(100);
        mSeekBar.setProgress(0);

        mProgressBar = findViewById(R.id.indeterminateBar);

        titleTv.setText(audioMsg.getAuthor() + " - " + audioMsg.getTitle());

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    if (service.isPlaying()) {
                        service.pauseMedia();
                    } else {
                        service.playMedia(msgUrl);
                    }
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    service.stopMedia();
                }
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AudioMsgActivity.this);

                builder.setTitle("Download Message");
                builder.setMessage("Do you want to download the message?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("Download", audioMsg.getMsgUrl());
                                String uri = audioMsg.getTitle();
                                downloadMsg(uri);
                            }
                        });
                        t.start();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if (mBound && b) {
                    service.seekToNew(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private void downloadMsg(String uri) {
        final Intent intent = new Intent(AudioMsgActivity.this, DownloadManagerReceiver.class);

        File localFile = null;
//        try {
//            localFile = File.createTempFile(audioMsg.getTitle(), "mp3");
//        } catch (IOException e) {
//            Log.d("CreateFile", "Unable to create file");
//            return;
//        }
        localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), audioMsg.getTitle() + ".mp3");
        Log.d("CreateFile", "File created");

        downloadID = PRDownloader.download(audioMsg.getMsgUrl(), localFile.getAbsolutePath(), audioMsg.getTitle())
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        Log.d("Download", "Download start or resume");
                        Log.d("StartOrResume", "DownloadId: "+downloadID);
                        Bundle b = new Bundle();
                        intent.setAction("DOWNLOADMANAGER_BROADCAST");
                        b.putInt(DOWNLOAD_ID, downloadID);
                        b.putString("message", CANCEL_DOWNLOAD);
                        intent.putExtras(b);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                        builder.setContentTitle("Downloading "+audioMsg.getTitle())
                                .setSmallIcon(R.drawable.ic_action_download)
                                .setContentText("Download in progress")
                                .addAction(0, "Cancel", pendingIntent);
                        notificationManagerCompat.notify(12345, builder.build());
                    }
                }).setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        Log.d("Download", "Download paused");
                    }
                }).setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        int percent = (int) ((progress.currentBytes * 100)/progress.totalBytes);
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        Log.d("Download", "Download cancelled");
                        notificationManagerCompat.cancel(12345);
                    }
                }).start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        //builder = new NotificationCompat.Builder(getApplicationContext(), "DOWNLOAD_CHANNEL");
                        Intent intent = new Intent(getApplicationContext(), DownloadManagerReceiver.class);
                        intent.putExtra("message", OPEN_DOWNLOAD);
                        Intent dismissIntent = new Intent(getApplicationContext(), DownloadManagerReceiver.class);
                        intent.putExtra("message", DISMISS_NOTIFICATION);
                        intent.setAction("DOWNLOADMANAGER_BROADCAST");
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, dismissIntent, 0);
                        builder.setContentText("Download complete")
                                .addAction(0, "Open", pendingIntent)
                                .addAction(0, "Dismiss", dismissPendingIntent)
                                .setAutoCancel(true);
                        notificationManagerCompat.notify(12345, builder.build());
                        Log.d("Download", "Download complete");
                    }

                    @Override
                    public void onError(Error error) {
                        Log.d("Download", "Connection error?="+error.isConnectionError()+", Server error?="+error.isServerError());
                        Toast.makeText(AudioMsgActivity.this, "Unable to download, pls try again later", Toast.LENGTH_SHORT).show();
                        notificationManagerCompat.cancel(12345);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Close Message");
        builder.setMessage("Do you want to end the message?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                service.stopMedia();
                service.stopSelf();
                AudioMsgActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mReceiver = new StateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyMusicService.MEDIA_INTENT);
        registerReceiver(mReceiver, filter);

        mNetworkReceiver = new NetworkReceiver();
        IntentFilter mfilter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, mfilter);

        intent = new Intent(AudioMsgActivity.this, MyMusicService.class);
        intent.putExtra(MyMusicService.MUSIC_URL, msgUrl);
        intent.putExtra(MyMusicService.NETWORK_STATE, isConnected);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        startService(intent);

        mHandler = new Handler();
        mHandler.postDelayed(updateRunner, 1000);

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBound) {
            mHandler.removeCallbacks(updateRunner);
        }

        if (mBound) {
            unbindService(connection);
        }

        unregisterReceiver(mReceiver);
        unregisterReceiver(mNetworkReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PRDownloader.shutDown();
    }

    private void updateSeekBar() {
        if (service.getPlayingStatus()) {
            double current = service.getCurrentPosition();
            double total = service.getDuration();
            if (total>0) {
                int temp = (int) ((current/total) * 100);

                mSeekBar.setProgress(temp);

                String currentString = convertToMinutes(service.getCurrentPosition());
                String totalString = convertToMinutes(service.getDuration());

                playedTv.setText(currentString);
                totalTv.setText(totalString);
                totalTime = totalString;
            }
        }
    }

    private String convertToMinutes(int time) {
        String modTime = "";
        int temp = time/1000;

        int minutes = temp/60;
        int seconds = temp%60;

        if (seconds<10) {
            modTime = String.valueOf(minutes)+":0"+String.valueOf(seconds);
        } else {
            modTime = String.valueOf(minutes)+":"+String.valueOf(seconds);
        }

        return modTime;
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyMusicService.MusicBinder binder = (MyMusicService.MusicBinder) iBinder;
            service = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    public class StateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");

            if (state.equals(MyMusicService.ACTION_PLAY)) {
                playButton.setImageResource(R.drawable.ic_action_pause);
            } else if (state.equals(MyMusicService.ACTION_PAUSE)) {
                playButton.setImageResource(R.drawable.ic_action_play);
            } else if (state.equals(MyMusicService.ACTION_STOP)) {
                playButton.setImageResource(R.drawable.ic_action_play);
                mSeekBar.setProgress(0);
                playedTv.setText("0:00");
                totalTv.setText(totalTime);
            } else if (state.equals(MyMusicService.ACTION_ERROR)) {
                Toast.makeText(AudioMsgActivity.this, "Server Error. Pls try again later", Toast.LENGTH_SHORT).show();
            } else if (state.equals(MyMusicService.CONNECTION_ERROR)) {
                Toast.makeText(AudioMsgActivity.this, "Unable to play because you are not connected. Pls check your internet connection", Toast.LENGTH_SHORT).show();
            } else if (state.equals(MyMusicService.IS_BUFFERING)) {
                mProgressBar.setVisibility(View.VISIBLE);
            } else if (state.equals(MyMusicService.NOT_BUFFERING)) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("state");

            final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                // notify user you are online
                isConnected = true;
                intent.putExtra(MyMusicService.MUSIC_URL, msgUrl);
                intent.putExtra(MyMusicService.NETWORK_STATE, isConnected);
                startService(intent);
            } else {
                // notify user you are not online
                isConnected = false;
                intent.putExtra(MyMusicService.MUSIC_URL, msgUrl);
                intent.putExtra(MyMusicService.NETWORK_STATE, isConnected);
                startService(intent);
            }
        }
    }

    private Runnable updateRunner = new Runnable() {
        @Override
        public void run() {
            if (mBound) {
                Log.d("current time", "Update Runner running:"+service.getBufferedPercent());
                updateSeekBar();
                mHandler.postDelayed(updateRunner, 1000);
            }
        }
    };
}
