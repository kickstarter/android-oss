package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.models.Video;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.viewmodels.VideoViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(VideoViewModel.class)
public final class VideoActivity extends BaseActivity<VideoViewModel> {
  private KSVideoPlayer player;
  private Video video;

  protected @Bind(R.id.video_player_layout) View rootView;
  protected @Bind(R.id.surface_view) SurfaceView surfaceView;
  protected @Bind(R.id.loading_indicator) ProgressBar loadingIndicatorProgressBar;
  protected @Bind(R.id.video_frame) AspectRatioFrameLayout videoFrame;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.video_player_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(IntentKey.PROJECT);
    video = project.video();

    viewModel.outputs.playerIsPrepared()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setPlayer);
  }

  @Override
  public void onDestroy() {
    viewModel.inputs.playerNeedsRelease(player);
    super.onDestroy();
  }

  @Override
  public void onResume() {
    super.onResume();
    viewModel.inputs.playerNeedsPrepare(video, surfaceView, rootView);
  }

  @Override
  public void onPause() {
    super.onPause();
    viewModel.inputs.playerNeedsRelease(player);
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

  private void setPlayer(final @NonNull KSVideoPlayer player) {
    this.player = player;
  }
}
