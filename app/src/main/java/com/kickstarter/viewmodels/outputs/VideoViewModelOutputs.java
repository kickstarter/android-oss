package com.kickstarter.viewmodels.outputs;

import com.kickstarter.libs.KSVideoPlayer;

import rx.Observable;

public interface VideoViewModelOutputs {
  Observable<Long> playerPositionOutput();
  Observable<KSVideoPlayer> playerIsPrepared();
}
