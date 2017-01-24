package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.models.Project;

import rx.Observable;

public interface ProjectViewModelOutputs {
  /**
   * Emits a project and country when a new value is available. If the view model is created with a full project
   * model, this observable will emit that project immediately, and then again when it has updated from the api.
   */
  Observable<Pair<Project, String>> projectAndUserCountry();

  /**
   * Emits when the success prompt for starring should be displayed.
   */
  Observable<Void> showStarredPrompt();

  /**
   * Emits when a login prompt should be displayed.
   */
  Observable<Void> showLoginTout();

  Observable<Project> showShareSheet();
  Observable<Project> playVideo();
  Observable<Project> startCampaignWebViewActivity();
  Observable<Project> startCreatorBioWebViewActivity();
  Observable<Project> startProjectUpdatesActivity();
  Observable<Project> startCommentsActivity();
  Observable<Project> startCheckout();
  Observable<Project> startManagePledge();
  Observable<Project> startViewPledge();
}
