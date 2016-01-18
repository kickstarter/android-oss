package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import rx.Observable;

public interface DiscoveryViewModelOutputs {
  Observable<List<Project>> projects();
  Observable<DiscoveryParams> params();
  Observable<Boolean> openDrawer();
  Observable<Boolean> shouldShowOnboarding();
  Observable<DiscoveryParams> showFilters();
  Observable<Void> showLogin();
  Observable<Pair<Project, RefTag>> showProject();
  Observable<Void> showProfile();
}
