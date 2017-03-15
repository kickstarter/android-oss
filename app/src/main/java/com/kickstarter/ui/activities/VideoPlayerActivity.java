package com.kickstarter.ui.activities;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSRendererBuilder;
import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.viewmodels.VideoViewModel;
import com.trello.rxlifecycle.ActivityEvent;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresActivityViewModel(VideoViewModel.ViewModel.class)
public final class VideoPlayerActivity extends BaseActivity<VideoViewModel.ViewModel> implements KSVideoPlayer.Listener {
  private MediaController mediaController;
  private KSVideoPlayer player;
  private long playerPosition;

  protected @Bind(R.id.video_player_layout) View rootView;
  protected @Bind(R.id.surface_view) SurfaceView surfaceView;
  protected @Bind(R.id.loading_indicator) ProgressBar loadingIndicatorProgressBar;
  protected @Bind(R.id.video_frame) AspectRatioFrameLayout videoFrame;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.video_player_layout);
    ButterKnife.bind(this);

    viewModel.outputs.preparePlayerWithUrl()
      .compose(Transformers.takeWhen(lifecycle().filter(ActivityEvent.RESUME::equals)))
      .compose(bindToLifecycle())
      .subscribe(this::preparePlayer);

    mediaController = new MediaController(this);
    mediaController.setAnchorView(rootView);

    RxView.clicks(rootView)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> toggleControlsVisibility());
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    releasePlayer();
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
      rootView.setSystemUiVisibility(systemUIFlags());
    }
  }

  @TargetApi(19)
  private int systemUIFlags() {
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
      | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
      | View.SYSTEM_UI_FLAG_FULLSCREEN;

    return ApiCapabilities.canSetImmersiveSystemUI()
      ? flags | View.SYSTEM_UI_FLAG_IMMERSIVE
      : flags;
  }

  private void releasePlayer() {
    if (player != null) {
      playerPosition = player.getCurrentPosition();
      player.release();
      player = null;
    }
  }

  public void preparePlayer(final @NonNull String videoUrl) {
    // Create player
    player = new KSVideoPlayer(new KSRendererBuilder(this, videoUrl));
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
      if (isMediaControllerAttachedToWindow()) {
        // Attempt fix for crash reports from Remix Mini / 5.1 where the media controller is attached to a window
        // but not showing. Adding it again crashes the app, so return to avoid that.
        return;
      }

      mediaController.show();
    }
  }

  @TargetApi(19)
  private boolean isMediaControllerAttachedToWindow() {
    return ApiCapabilities.canCheckMediaControllerIsAttachedToWindow() && mediaController.isAttachedToWindow();
  }
}
