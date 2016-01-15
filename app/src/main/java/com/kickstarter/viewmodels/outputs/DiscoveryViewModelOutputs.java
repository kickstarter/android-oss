package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import rx.Observable;

public interface DiscoveryViewModelOutputs {
  Observable<List<Project>> projects();
  Observable<DiscoveryParams> params();
  Observable<Activity> activity();
  Observable<Boolean> shouldShowOnboarding();
  Observable<DiscoveryParams> showFilters();
  Observable<Pair<Project, RefTag>> showProject();
  Observable<Void> showSignupLogin();
  Observable<Void> showActivityFeed();
  Observable<Activity> showActivityUpdate();
  Observable<Boolean> shouldShowActivitySample();
}
