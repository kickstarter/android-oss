package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.FeatureKey;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.DiscoveryDrawerUtils;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.WebClientType;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.intentmappers.DiscoveryIntentMapper;
import com.kickstarter.ui.intentmappers.IntentMapper;
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder;
import com.kickstarter.viewmodels.inputs.DiscoveryViewModelInputs;
import com.kickstarter.viewmodels.outputs.DiscoveryViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.utils.ObjectUtils.coalesce;

public final class DiscoveryViewModel extends ActivityViewModel<DiscoveryActivity> implements DiscoveryViewModelInputs,
  DiscoveryViewModelOutputs {
  private final ApiClientType apiClient;
  private final WebClientType webClient;
  private final BuildCheck buildCheck;
  private final CurrentUserType currentUser;

  public DiscoveryViewModel(final @NonNull Environment environment) {
    super(environment);

    apiClient = environment.apiClient();
    buildCheck = environment.buildCheck();
    currentUser = environment.currentUser();
    webClient = environment.webClient();

    buildCheck.bind(this, webClient);

    showBuildCheckAlert = newerBuildIsAvailable;
    showInternalTools = internalToolsClick;
    showLoginTout = loggedOutLoginToutClick;
    showProfile = profileClick;
    showSettings = settingsClick;

    creatorDashboardButtonIsGone = environment.currentConfig().observable()
      .map(config -> !coalesce(config.features().get(FeatureKey.ANDROID_CREATOR_VIEW), false));

    // Seed params when we are freshly launching the app with no data.
    final Observable<DiscoveryParams> paramsFromInitialIntent = intent()
      .take(1)
      .map(Intent::getAction)
      .filter(Intent.ACTION_MAIN::equals)
      .map(__ -> DiscoveryParams.builder().build())
      .share();

    final Observable<DiscoveryParams> paramsFromIntent = intent()
      .flatMap(i -> DiscoveryIntentMapper.params(i, apiClient));

    final Observable<DiscoveryParams> drawerParamsClicked = childFilterRowClick
      .mergeWith(topFilterRowClick)
      .map(NavigationDrawerData.Section.Row::params);

    // Merge various param data sources.
    final Observable<DiscoveryParams> params = Observable.merge(
      paramsFromInitialIntent,
      paramsFromIntent,
      drawerParamsClicked
    );

    final Observable<Integer> pagerSelectedPage = pagerSetPrimaryPage.distinctUntilChanged();

    // Combine params with the selected sort position.
    Observable.combineLatest(
      params,
      pagerSelectedPage.map(DiscoveryUtils::sortFromPosition),
      (p, s) -> p.toBuilder().sort(s).build()
    )
      .compose(bindToLifecycle())
      .subscribe(updateParamsForPage);

    final Observable<List<Category>> categories = apiClient.fetchCategories()
      .compose(neverError())
      .flatMap(Observable::from)
      .toSortedList()
      .share();

    // Combine root categories with the selected sort position.
    Observable.combineLatest(
      categories
        .flatMap(Observable::from)
        .filter(Category::isRoot)
        .toList(),
      pagerSelectedPage,
      Pair::create
    )
      .compose(bindToLifecycle())
      .subscribe(rootCategoriesAndPosition);

    final Observable<Category> drawerClickedParentCategory = parentFilterRowClick
      .map(NavigationDrawerData.Section.Row::params)
      .map(DiscoveryParams::category);

    final Observable<Category> expandedCategory = Observable.merge(
        topFilterRowClick.map(__ -> (Category) null),
        drawerClickedParentCategory
      )
      .scan(null, (previous, next) -> {
        if (previous != null && next != null && previous.equals(next)) {
          return null;
        }
        return next;
      });

    // Accumulate a list of pages to clear when the params or user changes,
    // to avoid displaying old data.
    pagerSelectedPage
      .compose(takeWhen(params))
      .compose(combineLatestPair(currentUser.observable()))
      .map(pageAndUser -> pageAndUser.first)
      .flatMap(currentPage -> Observable.from(DiscoveryParams.Sort.values())
        .map(DiscoveryUtils::positionFromSort)
        .filter(sortPosition -> !sortPosition.equals(currentPage))
        .toList()
      )
      .compose(bindToLifecycle())
      .subscribe(clearPages);

    params.distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(updateToolbarWithParams);

    updateParamsForPage.map(__ -> true)
      .compose(bindToLifecycle())
      .subscribe(expandSortTabLayout);

    Observable.combineLatest(
      categories,
      params,
      expandedCategory,
      currentUser.observable(),
      DiscoveryDrawerUtils::deriveNavigationDrawerData
    )
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(navigationDrawerData);

    drawerParamsClicked
      .compose(bindToLifecycle())
      .subscribe(koala::trackDiscoveryFilterSelected);

    Observable.merge(
      openDrawer,
      childFilterRowClick.map(__ -> false),
      topFilterRowClick.map(__ -> false),
      internalToolsClick.map(__ -> false),
      loggedOutLoginToutClick.map(__ -> false),
      profileClick.map(__ -> false),
      settingsClick.map(__ -> false)
    )
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(drawerIsOpen);

    openDrawer
      .filter(BooleanUtils::isTrue)
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackDiscoveryFilters());

    intent()
      .filter(IntentMapper::appBannerIsSet)
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackOpenedAppBanner());
  }

  private final PublishSubject<NavigationDrawerData.Section.Row> childFilterRowClick = PublishSubject.create();
  private final PublishSubject<Void> internalToolsClick = PublishSubject.create();
  private final PublishSubject<Void> loggedOutLoginToutClick = PublishSubject.create();
  private final PublishSubject<InternalBuildEnvelope> newerBuildIsAvailable = PublishSubject.create();
  private final PublishSubject<Boolean> openDrawer = PublishSubject.create();
  private final PublishSubject<Integer> pagerSetPrimaryPage = PublishSubject.create();
  private final PublishSubject<NavigationDrawerData.Section.Row> parentFilterRowClick = PublishSubject.create();
  private final PublishSubject<Void> profileClick = PublishSubject.create();
  private final PublishSubject<Void> settingsClick = PublishSubject.create();
  private final PublishSubject<NavigationDrawerData.Section.Row> topFilterRowClick = PublishSubject.create();

  private final Observable<Boolean> creatorDashboardButtonIsGone;
  private final BehaviorSubject<List<Integer>> clearPages = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> drawerIsOpen = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> expandSortTabLayout = BehaviorSubject.create();
  private final BehaviorSubject<NavigationDrawerData> navigationDrawerData = BehaviorSubject.create();
  private final BehaviorSubject<Pair<List<Category>, Integer>> rootCategoriesAndPosition = BehaviorSubject.create();
  private final Observable<InternalBuildEnvelope> showBuildCheckAlert;
  private final Observable<Void> showInternalTools;
  private final Observable<Void> showLoginTout;
  private final Observable<Void> showProfile;
  private final Observable<Void> showSettings;
  private final BehaviorSubject<DiscoveryParams> updateParamsForPage = BehaviorSubject.create();
  private final BehaviorSubject<DiscoveryParams> updateToolbarWithParams = BehaviorSubject.create();

  public final DiscoveryViewModelInputs inputs = this;
  public final DiscoveryViewModelOutputs outputs = this;

  @Override public void childFilterViewHolderRowClick(final @NonNull ChildFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row) {
    childFilterRowClick.onNext(row);
  }
  @Override public void discoveryPagerAdapterSetPrimaryPage(final @NonNull DiscoveryPagerAdapter adapter, final int position) {
    pagerSetPrimaryPage.onNext(position);
  }
  @Override public void loggedInViewHolderInternalToolsClick(final @NonNull LoggedInViewHolder viewHolder) {
    internalToolsClick.onNext(null);
  }
  @Override public void loggedInViewHolderProfileClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user) {
    profileClick.onNext(null);
  }
  @Override public void loggedInViewHolderSettingsClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user) {
    settingsClick.onNext(null);
  }
  @Override public void loggedOutViewHolderInternalToolsClick(final @NonNull LoggedOutViewHolder viewHolder) {
    internalToolsClick.onNext(null);
  }
  @Override public void loggedOutViewHolderLoginToutClick(final @NonNull LoggedOutViewHolder viewHolder) {
    loggedOutLoginToutClick.onNext(null);
  }
  @Override public void newerBuildIsAvailable(final @NonNull InternalBuildEnvelope envelope) {
    newerBuildIsAvailable.onNext(envelope);
  }
  @Override public void openDrawer(final boolean open) {
    openDrawer.onNext(open);
  }
  @Override public void parentFilterViewHolderRowClick(final @NonNull ParentFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row) {
    parentFilterRowClick.onNext(row);
  }
  @Override public void topFilterViewHolderRowClick(final @NonNull TopFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row) {
    topFilterRowClick.onNext(row);
  }

  @Override public @NonNull Observable<List<Integer>> clearPages() {
    return clearPages;
  }
  @Override public @NonNull Observable<Boolean> creatorDashboardButtonIsGone() {
    return creatorDashboardButtonIsGone;
  }
  @Override public @NonNull Observable<Boolean> drawerIsOpen() {
    return drawerIsOpen;
  }
  @Override public @NonNull Observable<Boolean> expandSortTabLayout() {
    return expandSortTabLayout;
  }
  @Override public @NonNull Observable<NavigationDrawerData> navigationDrawerData() {
    return navigationDrawerData;
  }
  @Override public @NonNull Observable<Pair<List<Category>, Integer>> rootCategoriesAndPosition() {
    return rootCategoriesAndPosition;
  }
  @Override public @NonNull Observable<InternalBuildEnvelope> showBuildCheckAlert() {
    return showBuildCheckAlert;
  }
  @Override public @NonNull Observable<Void> showInternalTools() {
    return showInternalTools;
  }
  @Override public @NonNull Observable<Void> showLoginTout() {
    return showLoginTout;
  }
  @Override public @NonNull Observable<Void> showProfile() {
    return showProfile;
  }
  @Override public @NonNull Observable<Void> showSettings() {
    return showSettings;
  }
  @Override public @NonNull Observable<DiscoveryParams> updateParamsForPage() {
    return updateParamsForPage;
  }
  @Override public @NonNull Observable<DiscoveryParams> updateToolbarWithParams() {
    return updateToolbarWithParams;
  }
}
