package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Update;

import rx.Observable;

public interface ProjectUpdatesViewModelOutputs {
  /**
   * Emits an update to start the comments activity with.
   */
  Observable<Update> startCommentsActivity();

  /**
   * Emits a web view url to display.
   */
  Observable<String> webViewUrl();
}
