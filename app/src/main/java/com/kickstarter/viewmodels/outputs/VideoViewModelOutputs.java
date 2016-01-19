package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface VideoViewModelOutputs {
  Observable<Integer> playbackState();
}
