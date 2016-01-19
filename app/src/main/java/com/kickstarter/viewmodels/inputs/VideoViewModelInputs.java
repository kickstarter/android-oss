package com.kickstarter.viewmodels.inputs;

import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;

import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.models.Video;

public interface VideoViewModelInputs {
  void loadingIndicator(ProgressBar progressBar);
  void playerNeedsPrepare(Video video, SurfaceView surfaceView, View rootView);
  void playerNeedsRelease(KSVideoPlayer player);
}
