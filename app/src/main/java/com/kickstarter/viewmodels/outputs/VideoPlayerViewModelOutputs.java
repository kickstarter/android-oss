package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Video;

import rx.Observable;

public interface VideoPlayerViewModelOutputs {
  Observable<Video> video();
}
