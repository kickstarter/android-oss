package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KsrRendererBuilder;
import com.kickstarter.models.Project;
import com.kickstarter.models.Video;
import com.kickstarter.libs.KsrVideoPlayer;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoPlayerActivity extends BaseActivity implements SurfaceHolder.Callback, KsrVideoPlayer.Listener {
  private MediaController mediaController;
  private KsrVideoPlayer player;
  private long playerPosition;
  private Video video;

  public @Bind(R.id.video_player_layout) View rootView;
  public @Bind(R.id.surface_view) SurfaceView surfaceView;
  public @Bind(R.id.loading_indicator) ProgressBar loadingIndicatorProgressBar;
  public @Bind(R.id.video_frame) AspectRatioFrameLayout videoFrame;

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.video_player_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(getString(R.string.intent_project));
    video = project.video();

    rootView.setOnTouchListener(((view, motionEvent) -> {
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        toggleControlsVisibility();
      }
      return true;
    }));

    surfaceView.getHolder().addCallback(this);
    mediaController = new MediaController(this);
    mediaController.setAnchorView(rootView);
  }

  private void preparePlayer(final boolean playWhenReady) {
    player = new KsrVideoPlayer(new KsrRendererBuilder(this, video.high()));
    player.setListener(this);
    player.seekTo(playerPosition);
    mediaController.setMediaPlayer(player.getPlayerControl());
    mediaController.setEnabled(true);

    player.prepare();
    player.setSurface(surfaceView.getHolder().getSurface());
    player.setPlayWhenReady(playWhenReady);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    releasePlayer();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (player == null) {
      preparePlayer(true);
    }
  }

  @Override
  public void onStateChanged(final boolean plaWhenReady, final int playbackState) {
    if (playbackState == ExoPlayer.STATE_ENDED) {
      mediaController.show();
    }

    if (playbackState == ExoPlayer.STATE_BUFFERING) {
      loadingIndicatorProgressBar.setVisibility(View.VISIBLE);
    } else {
      loadingIndicatorProgressBar.setVisibility(View.GONE);
    }
  }

  private void releasePlayer() {
    if (player != null) {
      playerPosition = player.getCurrentPosition();
      player.release();
      player = null;
    }
  }

  @Override
  public void surfaceCreated(@NonNull final SurfaceHolder surfaceHolder) {
    if (player != null) {
      player.setSurface(surfaceView.getHolder().getSurface());
    }
  }

  @Override
  public void surfaceChanged(@NonNull final SurfaceHolder surfaceHolder, final int format, final int width,
    final int height) {
    // Do nothing for now.
  }

  @Override
  public void surfaceDestroyed(@NonNull final SurfaceHolder surfaceHolder) {
    if (player != null) {
      surfaceHolder.getSurface().release();
      player.pushSurface(true);
    }
  }

  public void toggleControlsVisibility() {
    if (mediaController.isShowing()) {
      mediaController.hide();
    } else {
      mediaController.show(0);
    }
  }
}
