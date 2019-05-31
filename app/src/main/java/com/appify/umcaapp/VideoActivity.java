package com.appify.umcaapp;

import android.os.Bundle;

import com.appify.umcaapp.model.Video;
import com.appify.umcaapp.utils.YoutubeConfig;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class VideoActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    private YouTubePlayerView youtubePlayerView;
    YouTubePlayer.OnInitializedListener onInitializedListener;
    private Video video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        youtubePlayerView = (YouTubePlayerView) findViewById(R.id.youtube_view);

        video = (Video) getIntent().getSerializableExtra("video");

        youtubePlayerView.initialize(YoutubeConfig.getRestrictedKey(), this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.loadVideo(video.getId());
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
}
