package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

import rx.Observable;

public interface ProjectViewModelOutputs {
  /**
   * Emits a project and config when a new value is available.
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
  Observable<Project> showCampaign();
  Observable<Project> showCreator();
  Observable<Project> showUpdates();
  Observable<Project> showComments();
  Observable<Project> startCheckout();
  Observable<Project> startManagePledge();
  Observable<Project> startViewPledge();
  Observable<Pair<Project, Reward>> startCheckoutWithReward();

}
