package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface ThanksViewModelOutputs {
  /**
   * Emits a project name to confirm the project that was backed.
   */
  Observable<String> projectName();
}
