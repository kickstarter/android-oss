package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSRendererBuilder;
import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.models.Project;
import com.kickstarter.models.Video;
import com.kickstarter.ui.IntentKey;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class VideoPlayerActivity extends BaseActivity implements KSVideoPlayer.Listener {
  private MediaController mediaController;
  private KSVideoPlayer player;
  private long playerPosition;
  private Video video;

  public @Bind(R.id.video_player_layout) View rootView;
  public @Bind(R.id.surface_view) SurfaceView surfaceView;
  public @Bind(R.id.loading_indicator) ProgressBar loadingIndicatorProgressBar;
  public @Bind(R.id.video_frame) AspectRatioFrameLayout videoFrame;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.video_player_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(IntentKey.PROJECT);
    video = project.video();

    mediaController = new MediaController(this);
    mediaController.setAnchorView(rootView);

    rootView.setOnTouchListener(((view, motionEvent) -> {
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        toggleControlsVisibility();
      }
      return true;
    }));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    releasePlayer();
  }

  @Override
  public void onResume() {
    super.onResume();
    preparePlayer();
  }

  @Override
  public void onPause() {
    super.onPause();
    releasePlayer();
  }

  @Override
  public void onStateChanged(final boolean playWhenReady, final int playbackState) {
    if (playbackState == ExoPlayer.STATE_ENDED) {
      finish();
    }

    if (playbackState == ExoPlayer.STATE_BUFFERING) {
      loadingIndicatorProgressBar.setVisibility(View.VISIBLE);
    } else {
      loadingIndicatorProgressBar.setVisibility(View.GONE);
    }
  }

  @Override
  public void onWindowFocusChanged(final boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);

    if (hasFocus) {
      rootView.setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
          | View.SYSTEM_UI_FLAG_FULLSCREEN
          | View.SYSTEM_UI_FLAG_IMMERSIVE
      );
    }
  }

  private void releasePlayer() {
    if (player != null) {
      playerPosition = player.getCurrentPosition();
      player.release();
      player = null;
    }
  }

  public void preparePlayer() {
    // Create player
    player = new KSVideoPlayer(new KSRendererBuilder(this, video.high()));
    player.setListener(this);
    player.seekTo(playerPosition);  // todo: will be used for inline video playing

    // Set media controller
    mediaController.setMediaPlayer(player.getPlayerControl());
    mediaController.setEnabled(true);

    player.prepare();
    player.setSurface(surfaceView.getHolder().getSurface());
    player.setPlayWhenReady(true);
  }

  public void toggleControlsVisibility() {
    if (mediaController.isShowing()) {
      mediaController.hide();
    } else {
      mediaController.show();
    }
  }
}
