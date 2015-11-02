package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.models.Project;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoPlayerActivity extends BaseActivity implements ExoPlayer.Listener {
  private MediaController mediaController;

  public @Bind(R.id.root) View root;
  public @Bind(R.id.video_frame) AspectRatioFrameLayout videoFrame;
  public @Bind(R.id.surface_view) SurfaceView surfaceView;

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.video_player_layout);
    ButterKnife.bind(this);

    root.setOnTouchListener(((view, motionEvent) -> {
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        Log.d("TEST", "touch");
      } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
        view.performClick();
      }
      return true;
    }));

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(getString(R.string.intent_project));
  }

  @Override
  public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {

  }

  @Override
  public void onPlayWhenReadyCommitted() {

  }

  @Override
  public void onPlayerError(@Nullable final ExoPlaybackException error) {

  }
}
