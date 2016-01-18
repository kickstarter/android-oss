package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;

import java.util.List;

import rx.Observable;

public interface DiscoveryViewModelOutputs {
  Observable<NavigationDrawerData> navigationDrawerData();
  Observable<Boolean> openDrawer();
  Observable<List<Project>> projects();
  Observable<DiscoveryParams> params();
  Observable<Boolean> shouldShowOnboarding();
  Observable<DiscoveryParams> showFilters();
  Observable<Void> showLogin();
  Observable<Pair<Project, RefTag>> showProject();
  Observable<Void> showProfile();
  Observable<Void> showSettings();
}
