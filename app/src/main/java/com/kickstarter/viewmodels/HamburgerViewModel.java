package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.models.HamburgerNavigationData;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.HamburgerActivity;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class HamburgerViewModel extends ViewModel<HamburgerActivity> {
  BehaviorSubject<HamburgerNavigationData> hamburgerNavigationData = BehaviorSubject.create();
  public @NonNull
  Observable<HamburgerNavigationData> hamburgerNavigationData() {
    return hamburgerNavigationData;
  }

  protected @Inject ApiClientType apiClient;
  protected @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    addSubscription(
      currentUser.observable()
        .map(u -> HamburgerNavigationData.builder().user(u).topFilters(topFilters()).build())
        .subscribe(hamburgerNavigationData::onNext)
    );
  }

  private @NonNull List<DiscoveryParams> topFilters() {
    return Arrays.asList(
      DiscoveryParams.builder().staffPicks(true).build(),
      DiscoveryParams.builder().starred(1).build(),
      DiscoveryParams.builder().build()
    );
  }
}
