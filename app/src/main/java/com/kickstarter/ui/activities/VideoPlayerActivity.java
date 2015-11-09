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
import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.libs.KsrRendererBuilder;
import com.kickstarter.models.Project;
import com.kickstarter.models.Video;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class VideoPlayerActivity extends BaseActivity implements KSVideoPlayer.Listener {
  private MediaController mediaController;
  private KSVideoPlayer player;
  private long playerPosition;

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
    final Video video = project.video();

    rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    rootView.setOnTouchListener(((view, motionEvent) -> {
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        toggleControlsVisibility();
      }
      return true;
    }));

    // Create player
    player = new KSVideoPlayer(new KsrRendererBuilder(this, video.high()));
    player.setListener(this);
    player.seekTo(playerPosition);  // todo: will be used for inline video playing

    // Set media controller
    mediaController = new MediaController(this);
    mediaController.setMediaPlayer(player.getPlayerControl());
    mediaController.setAnchorView(rootView);
    mediaController.setEnabled(true);

    player.prepare();
    player.setSurface(surfaceView.getHolder().getSurface());
    player.setPlayWhenReady(true);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    releasePlayer();
  }

  @Override
  public void onStateChanged(final boolean playWhenReady, final int playbackState) {
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
    playerPosition = player.getCurrentPosition();
    player.release();
    player = null;
  }

  public void toggleControlsVisibility() {
    if (mediaController.isShowing()) {
      mediaController.hide();
    } else {
      mediaController.show(0);
    }
  }
}
