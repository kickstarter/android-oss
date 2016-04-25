package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface ProjectNotificationViewModelOutputs {
  /**
   * Emits the project's name.
   */
  Observable<String> projectName();

  /**
   * Emits `True` if the enabled switch should be toggled on, `False` otherwise.
   */
  Observable<Boolean> enabledSwitch();
}
