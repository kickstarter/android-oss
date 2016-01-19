package com.kickstarter.viewmodels.inputs;

import android.view.SurfaceView;
import android.view.View;

import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.models.Video;

public interface VideoViewModelInputs {
  void playerNeedsPrepare(Video video, SurfaceView surfaceView, View rootView);
  void playerNeedsRelease();
  void videoPaused();
  void videoStopped();
}
