package com.kickstarter.viewmodels.inputs;

import android.view.SurfaceView;
import android.view.View;

import com.kickstarter.libs.KSVideoPlayer;
import com.kickstarter.models.Video;

public interface VideoViewModelInputs {
  void playerNeedsPrepare(Video video, long position, SurfaceView surfaceView, View rootView);
  void playerNeedsRelease(KSVideoPlayer player);
}
