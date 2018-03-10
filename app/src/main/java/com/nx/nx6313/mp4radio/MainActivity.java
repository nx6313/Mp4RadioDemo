package com.nx.nx6313.mp4radio;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private VideoPlayer videoPlayer;
    Session session = Session.getSession();
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session.initialize(this);

        videoPlayer = findViewById(R.id.videoView);

        String playNameStr = this.getIntent().getStringExtra("playNameStr");
        String playUrlStr = this.getIntent().getStringExtra("playUrlStr");

        videoPlayer.setData(playNameStr, playUrlStr);
        videoPlayer.setPlayerActivity(MainActivity.this);
        videoPlayer.backFromFullScreen();

        videoPlayer.setBackClickListener(backClickListener);
        videoPlayer.setDownloadClickListener(downloadClickListener);
        videoPlayer.setFavoriteClickListener(favoriteClickListener);
    }

    @Override
    protected void onDestroy() {
        videoPlayer.onDestroy();
        super.onDestroy();
    }

    View.OnClickListener backClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (videoPlayer.isFullScreen()) {
                videoPlayer.backFromFullScreen();
            } else {
                onBackPressed();
            }
        }
    };

    View.OnClickListener favoriteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (videoPlayer.isFavorite()) {
                Toast.makeText(MainActivity.this, "取消收藏成功！", Toast.LENGTH_LONG).show();
                videoPlayer.setFavorite(false);
            } else {
                Toast.makeText(MainActivity.this, "收藏成功！", Toast.LENGTH_LONG).show();
                videoPlayer.setFavorite(true);
            }
        }
    };

    View.OnClickListener downloadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(MainActivity.this, "已经开始下载", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onPause() {
        if(videoPlayer != null) {
            videoPlayer.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(videoPlayer != null) {
            videoPlayer.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        if (videoPlayer.isFullScreen()) {
            videoPlayer.backFromFullScreen();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        videoPlayer.onConfigurationChanged(newConfig);
    }
}
