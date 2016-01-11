package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import rx.Observable;

public interface DiscoveryViewModelOutputs {
  Observable<List<Project>> projects();
  Observable<DiscoveryParams> params();
  Observable<List<Activity>> activities();
  Observable<Boolean> shouldShowOnboarding();
  Observable<DiscoveryParams> showFilters();
  Observable<Project> showProject();
  Observable<Void> showSignupLogin();
  Observable<Void> showActivityFeed();
  Observable<Activity> showActivityUpdate();
}
