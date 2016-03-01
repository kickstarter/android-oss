package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Project;

import rx.Observable;

public interface ThanksViewModelOutputs {
  /**
   * Emits the backing's project name.
   */
  Observable<String> projectName();

  /**
   * Start a new project activity.
   */
  Observable<Project> startProject();

  /**
   * Share the project using Android's app chooser.
   */
  Observable<Project> startShare();

  /**
   * Share the project on Facebook.
   */
  Observable<Project> startShareOnFacebook();

  /**
   * Share the project on Twitter.
   */
  Observable<Project> startShareOnTwitter();
}
