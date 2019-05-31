package com.appify.umcaapp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.appify.umcaapp.utils.Utilities;

import java.io.IOException;

public class MyMusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener {
    public static final String MUSIC_URL = "music_url";
    public static final String ACTION_PLAY = "played";
    public static final String ACTION_PAUSE = "paused";
    public static final String ACTION_STOP = "stopped";
    public static final String MEDIA_INTENT = "com.broadcast.intent";
    public static final String CONNECTION_ERROR = "connection error";
    public static final String ACTION_ERROR = "error";
    public static final String IS_BUFFERING = "buffering start";
    public static final String NOT_BUFFERING = "buffering end";
    public static final String NETWORK_STATE = "network state";

    private IBinder iBinder = new MusicBinder();
    private MediaPlayer mp = new MediaPlayer();

    private boolean isReady = false;
    private boolean isPaused = false;
    private boolean isConnected = false;
    private int bufferedPercent = 0;

    public MyMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra(MyMusicService.MUSIC_URL);
        isConnected = intent.getBooleanExtra(NETWORK_STATE, false);

        if (!mp.isPlaying() && !isPaused) {
            playMedia(url);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mp = new MediaPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mp.start();
        isReady = true;
        isPaused = false;
        Intent intent = new Intent(MEDIA_INTENT);
        intent.putExtra("state", ACTION_PLAY);
        sendBroadcast(intent);
    }

    public void pauseMedia() {
        mp.pause();
        Intent intent = new Intent(MEDIA_INTENT);
        intent.putExtra("state", ACTION_PAUSE);
        sendBroadcast(intent);
        isPaused = true;
    }

    public void playMedia(String url) {
        if (isPaused) {
            mp.start();
            Intent intent = new Intent(MEDIA_INTENT);
            intent.putExtra("state", ACTION_PLAY);
            sendBroadcast(intent);
        } else {
            try {
                if (isConnected) {
                    //User is connected, therefore set the datasource and prepare the media.
                    mp.reset();
                    mp.setDataSource(url);
                } else {
                    //Send message to activity that user is not connected to internet
                    Intent intent = new Intent(MEDIA_INTENT);
                    intent.putExtra("state", CONNECTION_ERROR);
                    sendBroadcast(intent);
                    return;
                }
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            mp.setOnPreparedListener(this);
            mp.setOnErrorListener(this);
            mp.setOnSeekCompleteListener(this);
            mp.setOnCompletionListener(this);
            mp.setOnBufferingUpdateListener(this);
            mp.setOnInfoListener(this);
            mp.prepareAsync();
        }
    }

    public void stopMedia() {
        if (mp.isPlaying() || isPaused) {
            mp.stop();
            isPaused = false;
            isReady = false;
            mp.reset();
            Intent intent = new Intent(MEDIA_INTENT);
            intent.putExtra("state", ACTION_STOP);
            sendBroadcast(intent);
        }
    }

    public void seekToNew(int newPos) {
        if (isReady) {
            Log.d("Proposed seek time", ""+newPos);
            if (bufferedPercent>newPos) {
                if (mp.isPlaying()) {
                    mp.pause();
                }

                int newCurrentPos = (newPos * mp.getDuration())/100;
                Log.d("Seek time", ""+newCurrentPos);

                mp.seekTo(newCurrentPos);
            }
        }
    }

    public void prevTrack() {

    }

    public int getDuration() {
        int dur = 0;

        if (mp != null) {
            dur = mp.getDuration();
        }
        return dur;
    }

    public int getCurrentPosition() {
        int dur = 0;
        if (mp!=null) {
            dur = mp.getCurrentPosition();
        }
        return dur;
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Toast.makeText(this, "Error while playing message", Toast.LENGTH_SHORT).show();

        if (i == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Intent intent = new Intent(MEDIA_INTENT);
            intent.putExtra("state", ACTION_ERROR);
            sendBroadcast(intent);
        }

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.reset();
        isReady = false;
        isPaused = false;
        Intent intent = new Intent(MEDIA_INTENT);
        intent.putExtra("state", ACTION_STOP);
        sendBroadcast(intent);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        bufferedPercent = i;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        if (i == MediaPlayer.MEDIA_INFO_BUFFERING_START && isReady) {
            Intent intent = new Intent(MEDIA_INTENT);
            intent.putExtra("state", IS_BUFFERING);
            sendBroadcast(intent);
        }

        if (i == MediaPlayer.MEDIA_INFO_BUFFERING_END && isReady) {
            Intent intent = new Intent(MEDIA_INTENT);
            intent.putExtra("state", NOT_BUFFERING);
            sendBroadcast(intent);
        }
        return false;
    }

    public class MusicBinder extends Binder {
        MyMusicService getService() {
            return MyMusicService.this;
        }
    }

    public boolean getPlayingStatus() {
        return isReady;
    }

    public boolean isPlaying() {
        if (isReady) {
            return mp.isPlaying();
        } else
            return false;
    }

    public int getBufferedPercent() {
        if (isReady)
            return bufferedPercent;
        else
            return 0;
    }
}
