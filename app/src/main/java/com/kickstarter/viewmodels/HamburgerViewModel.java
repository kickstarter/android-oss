package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Category;
import com.kickstarter.models.HamburgerNavigationData;
import com.kickstarter.models.HamburgerNavigationItem;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.HamburgerActivity;
import com.kickstarter.viewmodels.inputs.HamburgerViewModelInputs;
import com.kickstarter.viewmodels.outputs.HamburgerViewModelOutputs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public final class HamburgerViewModel extends ViewModel<HamburgerActivity> implements HamburgerViewModelInputs,
  HamburgerViewModelOutputs {

  // INPUTS
  PublishSubject<HamburgerNavigationItem> hamburgerNavigationItemClick = PublishSubject.create();
  @Override
  public void filterClicked(final @NonNull HamburgerNavigationItem item) {
    Timber.d("Filter clicked!");
    hamburgerNavigationItemClick.onNext(item);
  }

  // OUTPUTS
  BehaviorSubject<HamburgerNavigationData> hamburgerNavigationData = BehaviorSubject.create();
  @Override
  public @NonNull Observable<HamburgerNavigationData> hamburgerNavigationData() {
    return hamburgerNavigationData;
  }

  public final HamburgerViewModelInputs inputs = this;
  public final HamburgerViewModelOutputs outputs = this;

  protected @Inject ApiClientType apiClient;
  protected @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Category>> categories = apiClient.fetchCategories()
      .compose(Transformers.neverError())
      .share();

    final Observable<Pair<List<Category>, User>> categoriesAndUser = categories
      .compose(Transformers.combineLatestPair(currentUser.observable()));

    addSubscription(
      categoriesAndUser
        .compose(Transformers.takeWhen(hamburgerNavigationItemClick))
        .map(cu -> {
          return HamburgerNavigationData.builder()
            .categoryFilters(subCategoryFilters(cu.first))
            .user(cu.second)
            .topFilters(topFilters(cu.second)).build();
        })
        .subscribe(hamburgerNavigationData::onNext)
    );

    addSubscription(
      categoriesAndUser
        .map(cu -> {
          return HamburgerNavigationData.builder()
            .categoryFilters(categoryFilters(cu.first))
            .user(cu.second)
            .topFilters(topFilters(cu.second)).build();
        })
        .subscribe(hamburgerNavigationData::onNext)
    );
  }

  private @NonNull List<HamburgerNavigationItem> categoryFilters(final @NonNull List<Category> categories) {
    HamburgerNavigationItem musicCategoryFilter = HamburgerNavigationItem.builder().discoveryParams(DiscoveryParams.builder().build()).build();
    for (final Category category : categories) {
      if (category.name().equals("Music")) {
        musicCategoryFilter = HamburgerNavigationItem.builder().discoveryParams(DiscoveryParams.builder().category(category).build()).build();
      }
    }
    final List<HamburgerNavigationItem> categoryFilters = new ArrayList<HamburgerNavigationItem>();
    categoryFilters.add(musicCategoryFilter);
    return categoryFilters;
  }

  private @NonNull List<HamburgerNavigationItem> subCategoryFilters(final @NonNull List<Category> categories) {
    HamburgerNavigationItem musicCategoryFilter = HamburgerNavigationItem.builder().discoveryParams(DiscoveryParams.builder().build()).build();
    final List<HamburgerNavigationItem> musicSubCategoryFilters = new ArrayList<>();
    for (final Category category : categories) {
      if (category.name().equals("Music")) {
        musicCategoryFilter = HamburgerNavigationItem.builder().discoveryParams(DiscoveryParams.builder().category(category).build()).build();
      } else if (category.parent() != null && category.parent().name().equals("Music")) {
        musicSubCategoryFilters.add(HamburgerNavigationItem.builder().discoveryParams(DiscoveryParams.builder().category(category).build()).build());
      }
    }
    final List<HamburgerNavigationItem> categoryFilters = new ArrayList<HamburgerNavigationItem>();
    categoryFilters.add(musicCategoryFilter);
    categoryFilters.addAll(musicSubCategoryFilters);
    return categoryFilters;
  }

  private @NonNull List<HamburgerNavigationItem> topFilters(final @Nullable User user) {
    final List<HamburgerNavigationItem> topFilters = new ArrayList<HamburgerNavigationItem>();
    topFilters.add(HamburgerNavigationItem.builder().discoveryParams(DiscoveryParams.builder().staffPicks(true).build()).build());
    topFilters.add(HamburgerNavigationItem.builder().selected(true).discoveryParams(DiscoveryParams.builder().starred(1).build()).build());
    if (user != null) {
      topFilters.add(HamburgerNavigationItem.builder().discoveryParams(DiscoveryParams.builder().social(1).build()).build());
    }
    topFilters.add(HamburgerNavigationItem.builder().discoveryParams(DiscoveryParams.builder().build()).build());
    return topFilters;
  }
}
