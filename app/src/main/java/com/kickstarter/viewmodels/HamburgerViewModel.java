package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Category;
import com.kickstarter.models.HamburgerNavigationData;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.HamburgerActivity;

import java.util.ArrayList;
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

    final Observable<List<Category>> categories = apiClient.fetchCategories()
      .compose(Transformers.neverError())
      .share();

    addSubscription(
      categories
        .compose(Transformers.combineLatestPair(currentUser.observable()))
        .map(cu -> {
          return HamburgerNavigationData.builder()
            .categoryFilters(categoryFilters(cu.first))
            .user(cu.second)
            .topFilters(topFilters(cu.second)).build();
        })
        .subscribe(hamburgerNavigationData::onNext)
    );
  }

  private @NonNull List<DiscoveryParams> categoryFilters(final @NonNull List<Category> categories) {
    DiscoveryParams musicCategoryFilter = DiscoveryParams.builder().build();
    final List<DiscoveryParams> musicSubCategoryFilters = new ArrayList<>();
    for (final Category category : categories) {
      if (category.name().equals("Music")) {
        musicCategoryFilter = DiscoveryParams.builder().category(category).build();
      } else if (category.parent() != null && category.parent().name().equals("Music")) {
        musicSubCategoryFilters.add(DiscoveryParams.builder().category(category).build());
      }
    }
    final List<DiscoveryParams> categoryFilters = new ArrayList<DiscoveryParams>();
    categoryFilters.add(musicCategoryFilter);
    categoryFilters.addAll(musicSubCategoryFilters);
    return categoryFilters;
  }

  private @NonNull List<DiscoveryParams> topFilters(final @Nullable User user) {
    final List<DiscoveryParams> topFilters = new ArrayList<DiscoveryParams>();
    topFilters.add(DiscoveryParams.builder().staffPicks(true).build());
    topFilters.add(DiscoveryParams.builder().starred(1).build());
    if (user != null) {
      topFilters.add(DiscoveryParams.builder().social(1).build());
    }
    topFilters.add(DiscoveryParams.builder().build());
    return topFilters;
  }
}
