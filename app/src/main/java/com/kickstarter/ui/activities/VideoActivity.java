package com.kickstarter.ui.activities;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.viewmodels.VideoViewModel;
import com.trello.rxlifecycle.ActivityEvent;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresActivityViewModel(VideoViewModel.ViewModel.class)
public final class VideoActivity extends BaseActivity<VideoViewModel.ViewModel> implements KSVideoPlayer.Listener {
  private MediaController mediaController;
  private ExoPlayer player;
  private long playerPosition;

  protected @Bind(R.id.video_player_layout) View rootView;
  protected @Bind(R.id.player_view) SurfaceView playerView;
  protected @Bind(R.id.loading_indicator) ProgressBar loadingIndicatorProgressBar;
  protected @Bind(R.id.video_frame) AspectRatioFrameLayout videoFrame;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.video_player_layout);
    ButterKnife.bind(this);

    this.viewModel.outputs.preparePlayerWithUrl()
      .compose(Transformers.takeWhen(lifecycle().filter(ActivityEvent.RESUME::equals)))
      .compose(bindToLifecycle())
      .subscribe(this::preparePlayer);

    this.mediaController = new MediaController(this);
    this.mediaController.setAnchorView(this.rootView);

    RxView.clicks(this.rootView)
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
    if (playbackState == Player.STATE_ENDED) {
      finish();
    }

    if (playbackState == Player.STATE_BUFFERING) {
      this.loadingIndicatorProgressBar.setVisibility(View.VISIBLE);
    } else {
      this.loadingIndicatorProgressBar.setVisibility(View.GONE);
    }
  }

  @Override
  public void onWindowFocusChanged(final boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);

    if (hasFocus) {
      this.rootView.setSystemUiVisibility(systemUIFlags());
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
    if (this.player != null) {
      this.playerPosition = this.player.getCurrentPosition();
      this.player.release();
      this.player = null;
    }
  }

  public void preparePlayer(final @NonNull String videoUrl) {
    // Create player
    this.player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
//    this.player.setListener(this);
    this.player.seekTo(this.playerPosition);  // todo: will be used for inline video playing

    // Set media controller
//    this.
//    this.mediaController.setMediaPlayer(this.player.getMediaController());
//    this.mediaController.setEnabled(true);

    this.player.prepare(ExtractorMediaSource.Factory(new DefaultDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null))
      .createMediaSource(uri, handler, listener));
    this.player.setPlayWhenReady(true);
  }

  public void toggleControlsVisibility() {
    if (this.mediaController.isShowing()) {
      this.mediaController.hide();
    } else {
      if (isMediaControllerAttachedToWindow()) {
        // Attempt fix for crash reports from Remix Mini / 5.1 where the media controller is attached to a window
        // but not showing. Adding it again crashes the app, so return to avoid that.
        return;
      }

      this.mediaController.show();
    }
  }

  @TargetApi(19)
  private boolean isMediaControllerAttachedToWindow() {
    return ApiCapabilities.canCheckMediaControllerIsAttachedToWindow() && this.mediaController.isAttachedToWindow();
  }
}
