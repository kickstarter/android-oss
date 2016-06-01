package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
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
import com.kickstarter.ui.data.ActivityResult;
import com.kickstarter.ui.intentmappers.DiscoveryIntentMapper;
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder;
import com.kickstarter.viewmodels.inputs.DiscoveryViewModelInputs;
import com.kickstarter.viewmodels.outputs.DiscoveryViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public final class DiscoveryViewModel extends ActivityViewModel<DiscoveryActivity> implements DiscoveryViewModelInputs,
  DiscoveryViewModelOutputs {
  private final ApiClientType apiClient;
  private final WebClientType webClient;
  private final BuildCheck buildCheck;
  private final CurrentUserType currentUser;

  // INPUTS
  private final PublishSubject<Boolean> openDrawer = PublishSubject.create();
  @Override
  public void openDrawer(final boolean open) {
    openDrawer.onNext(open);
  }

  private final PublishSubject<Integer> pageChanged = PublishSubject.create();
  @Override
  public void pageChanged(final int position) {
    pageChanged.onNext(position);
  }

  // DiscoveryPagerAdapter.Delegate inputs
  private final PublishSubject<Integer> pagerCreatedPage = PublishSubject.create();
  @Override
  public void discoveryPagerAdapterCreatedPage(final DiscoveryPagerAdapter adapter, final int position) {
    pagerCreatedPage.onNext(position);
  }

  // NAVIGATION DRAWER DELEGATE INPUTS
  private PublishSubject<NavigationDrawerData.Section.Row> childFilterRowClick = PublishSubject.create();
  @Override
  public void childFilterViewHolderRowClick(final @NonNull ChildFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row) {
    childFilterRowClick.onNext(row);
  }

  private PublishSubject<Void> loggedOutLoginToutClick = PublishSubject.create();
  @Override
  public void loggedOutViewHolderLoginToutClick(final @NonNull LoggedOutViewHolder viewHolder) {
    loggedOutLoginToutClick.onNext(null);
  }

  private PublishSubject<NavigationDrawerData.Section.Row> parentFilterRowClick = PublishSubject.create();
  @Override
  public void parentFilterViewHolderRowClick(final @NonNull ParentFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row) {
    parentFilterRowClick.onNext(row);
  }

  private PublishSubject<InternalBuildEnvelope> newerBuildIsAvailable = PublishSubject.create();
  @Override
  public void newerBuildIsAvailable(final @NonNull InternalBuildEnvelope envelope) {
    newerBuildIsAvailable.onNext(envelope);
  }

  private PublishSubject<Void> internalToolsClick = PublishSubject.create();
  @Override
  public void loggedInViewHolderInternalToolsClick(final @NonNull LoggedInViewHolder viewHolder) {
    internalToolsClick.onNext(null);
  }

  @Override
  public void loggedOutViewHolderInternalToolsClick(final @NonNull LoggedOutViewHolder viewHolder) {
    internalToolsClick.onNext(null);
  }

  private PublishSubject<Void> profileClick = PublishSubject.create();
  @Override
  public void loggedInViewHolderProfileClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user) {
    profileClick.onNext(null);
  }

  private PublishSubject<Void> settingsClick = PublishSubject.create();
  @Override
  public void loggedInViewHolderSettingsClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user) {
    settingsClick.onNext(null);
  }

  private PublishSubject<NavigationDrawerData.Section.Row> topFilterRowClick = PublishSubject.create();
  @Override
  public void topFilterViewHolderRowClick(final @NonNull TopFilterViewHolder viewHolder, final @NonNull NavigationDrawerData.Section.Row row) {
    topFilterRowClick.onNext(row);
  }

  // OUTPUTS
  private final Observable<InternalBuildEnvelope> showBuildCheckAlert;
  @Override
  public Observable<InternalBuildEnvelope> showBuildCheckAlert() {
    return showBuildCheckAlert;
  }

  private final Observable<Void> showInternalTools;
  @Override
  public Observable<Void> showInternalTools() {
    return showInternalTools;
  }

  private final Observable<Void> showProfile;
  @Override
  public Observable<Void> showProfile() {
    return showProfile;
  }

  private final Observable<Void> showLoginTout;
  @Override
  public Observable<Void> showLoginTout() {
    return showLoginTout;
  }

  private final Observable<Void> showSettings;
  @Override
  public Observable<Void> showSettings() {
    return showSettings;
  }

  private BehaviorSubject<NavigationDrawerData> navigationDrawerData = BehaviorSubject.create();
  @Override
  public Observable<NavigationDrawerData> navigationDrawerData() {
    return navigationDrawerData;
  }

  private BehaviorSubject<Boolean> drawerIsOpen = BehaviorSubject.create();
  @Override
  public Observable<Boolean> drawerIsOpen() {
    return drawerIsOpen;
  }

  final BehaviorSubject<DiscoveryParams> updateToolbarWithParams = BehaviorSubject.create();
  @Override
  public Observable<DiscoveryParams> updateToolbarWithParams() {
    return updateToolbarWithParams;
  }

  final PublishSubject<Pair<DiscoveryParams, Integer>> updateParamsForPage = PublishSubject.create();
  @Override
  public Observable<Pair<DiscoveryParams, Integer>> updateParamsForPage() {
    return updateParamsForPage;
  }

  final PublishSubject<List<Integer>> clearPages = PublishSubject.create();
  public Observable<List<Integer>> clearPages() {
    return clearPages;
  }

  final BehaviorSubject<Boolean> expandSortTabLayout = BehaviorSubject.create();
  @Override
  public Observable<Boolean> expandSortTabLayout() {
    return expandSortTabLayout;
  }

  public final DiscoveryViewModelInputs inputs = this;
  public final DiscoveryViewModelOutputs outputs = this;

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

    // Seed params when we are freshly launching the app with no data.
    final Observable<DiscoveryParams> paramsFromInitialIntent = intent()
      .take(1)
      .map(Intent::getAction)
      .filter(Intent.ACTION_MAIN::equals)
      .map(__ -> DiscoveryParams.builder().staffPicks(true).build());

    final Observable<DiscoveryParams> paramsFromIntent = intent()
      .flatMap(i -> DiscoveryIntentMapper.params(i, apiClient));

    final Observable<DiscoveryParams> drawerParamsClicked = childFilterRowClick
      .mergeWith(topFilterRowClick)
      .map(NavigationDrawerData.Section.Row::params);

    // Merge various param data sources to one true selected params value.
    final Observable<DiscoveryParams> params = Observable.merge(
      paramsFromInitialIntent,
      paramsFromIntent,
      drawerParamsClicked
    );

    // Emit only the first event in which the view pager creates a page--we only care about the first creation event
    // since pages are only created in memory once.
    final Observable<DiscoveryParams.Sort> pagerSelectedSort = pagerCreatedPage
      // This needs to be observed on the main thread to handle a delay between when a fragment is constructed,
      // and when it is available via the FragmentManager. This is not a pattern we should repeat, need to consider
      // how to robustly avoid this race condition.
      .observeOn(AndroidSchedulers.mainThread())
      .take(1)
      // Start with page 0 since we skip the RxViewPager binding's immediate emission, which happens before
      // the adapter and fragment are ready. Map the resulting current pager position to its corresponding sort param.
      .switchMap(__ -> pageChanged.startWith(0))
      .map(DiscoveryUtils::sortFromPosition)
      .distinctUntilChanged();

    final Observable<DiscoveryParams> paramsOnSortChange = params
      .compose(takePairWhen(pagerSelectedSort))
      .map(ps -> ps.first.toBuilder().sort(ps.second).build());

    final Observable<DiscoveryParams> paramsOnDrawerSelection = pagerSelectedSort
      .compose(takePairWhen(drawerParamsClicked))
      .map(sp -> sp.second.toBuilder().sort(sp.first).build());

    final Observable<List<Category>> categories = apiClient.fetchCategories()
      .compose(neverError())
      .flatMap(Observable::from)
      .toSortedList()
      .share();

    final Observable<Category> drawerClickedParentCategory = parentFilterRowClick
      .map(NavigationDrawerData.Section.Row::params)
      .map(DiscoveryParams::category);

    final Observable<Category> expandedCategory = Observable.merge(
        topFilterRowClick.map(__ -> (Category) null),
        drawerClickedParentCategory
      )
      .scan((Category) null, (previous, next) -> {
        if (previous != null && next != null && previous.equals(next)) {
          return null;
        }
        return next;
      });

    // Accumulate a list of pages to clear when the params or user changes,
    // to avoid displaying old data.
    pageChanged
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

    Observable.merge(paramsOnSortChange, paramsOnDrawerSelection)
      .map(p -> Pair.create(p, DiscoveryUtils.positionFromSort(p.sort())))
      .compose(bindToLifecycle())
      .subscribe(updateParamsForPage);

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
  }

  private static boolean isSuccessfulLogin(final @NonNull ActivityResult activityResult) {
    return activityResult.isOk() && activityResult.isRequestCode(ActivityRequestCodes.LOGIN_FLOW);
  }
}
