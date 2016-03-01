package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Project;

import rx.Observable;

public interface ThanksViewModelOutputs {
  /**
   * Emits the backing's project name.
   */
  Observable<String> projectName();

  /**
   * Share the project using Android's app chooser.
   */
  Observable<Project> startShareIntent();

  /**
   * Share the project on Facebook.
   */
  Observable<Project> startShareOnFacebookIntent();

  /**
   * Share the project on Twitter.
   */
  Observable<Project> startShareOnTwitterIntent();
}
