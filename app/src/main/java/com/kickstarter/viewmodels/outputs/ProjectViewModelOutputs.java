package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.User;

import rx.Observable;

public interface ProjectViewModelOutputs {
  Observable<Pair<Project, User>> projectAndUser();

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

  Observable<Void> showStarredPrompt();
  Observable<Void> showLoginTout();
}
