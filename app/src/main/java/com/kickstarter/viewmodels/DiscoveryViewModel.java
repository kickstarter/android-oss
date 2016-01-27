package com.kickstarter.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.preferences.IntPreference;
import com.kickstarter.libs.qualifiers.ActivitySamplePreference;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.DiscoveryDrawerUtils;
import com.kickstarter.libs.utils.DiscoveryParamsUtils;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder;
import com.kickstarter.ui.data.ActivityResult;
import com.kickstarter.ui.intentmappers.DiscoveryIntentMapper;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder;
import com.kickstarter.viewmodels.inputs.DiscoveryViewModelInputs;
import com.kickstarter.viewmodels.outputs.DiscoveryViewModelOutputs;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public final class DiscoveryViewModel extends ViewModel<DiscoveryActivity> implements DiscoveryViewModelInputs,
  DiscoveryViewModelOutputs {
  protected @Inject ApiClientType apiClient;
  protected @Inject WebClient webClient;
  protected @Inject BuildCheck buildCheck;
  protected @Inject CurrentUser currentUser;
  protected @Inject @ActivitySamplePreference IntPreference activitySamplePreference;

  // INPUTS
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  @Override
  public void nextPage() {
    nextPage.onNext(null);
  }

  private final PublishSubject<Boolean> openDrawer = PublishSubject.create();
  @Override
  public void openDrawer(boolean open) {
    openDrawer.onNext(open);
  }

  // ONBOARDING DELEGATE INPUTS
  private PublishSubject<Void> discoveryOnboardingLoginToutClick = PublishSubject.create();
  @Override
  public void discoveryOnboardingViewHolderLoginToutClick(DiscoveryOnboardingViewHolder viewHolder) {
    discoveryOnboardingLoginToutClick.onNext(null);
  }

  // PROJECT VIEW HOLDER DELEGATE INPUTS
  private PublishSubject<Project> clickProject = PublishSubject.create();
  @Override
  public void projectCardViewHolderClick(ProjectCardViewHolder viewHolder, Project project) {
    clickProject.onNext(project);
  }

  // NAVIGATION DRAWER DELEGATE INPUTS
  private PublishSubject<NavigationDrawerData.Section.Row> childFilterRowClick = PublishSubject.create();
  @Override
  public void childFilterViewHolderRowClick(@NonNull ChildFilterViewHolder viewHolder, @NonNull NavigationDrawerData.Section.Row row) {
    childFilterRowClick.onNext(row);
  }

  private PublishSubject<Void> loggedOutLoginToutClick = PublishSubject.create();
  @Override
  public void loggedOutViewHolderLoginToutClick(final @NonNull LoggedOutViewHolder viewHolder) {
    discoveryOnboardingLoginToutClick.onNext(null);
  }

  private PublishSubject<NavigationDrawerData.Section.Row> parentFilterRowClick = PublishSubject.create();
  @Override
  public void parentFilterViewHolderRowClick(@NonNull ParentFilterViewHolder viewHolder, @NonNull NavigationDrawerData.Section.Row row) {
    parentFilterRowClick.onNext(row);
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
  public void topFilterViewHolderRowClick(@NonNull TopFilterViewHolder viewHolder, @NonNull NavigationDrawerData.Section.Row row) {
    topFilterRowClick.onNext(row);
  }

  // ACTIVITY SAMPLE DELEGATE INPUTS
  private PublishSubject<Project> clickActivityProject = PublishSubject.create();
  @Override
  public void activitySampleProjectViewHolderUpdateClicked(final @NonNull ActivitySampleProjectViewHolder viewHolder,
    final @NonNull Activity activity) {
    showActivityUpdate.onNext(activity);
  }

  @Override
  public void activitySampleProjectViewHolderProjectClicked(final @NonNull ActivitySampleProjectViewHolder viewHolder,
    final @NonNull Project project) {
    activitySampleProjectClicked(project);
  }

  @Override
  public void activitySampleFriendBackingViewHolderProjectClicked(final @NonNull ActivitySampleFriendBackingViewHolder viewHolder,
    final @NonNull Project project) {
    activitySampleProjectClicked(project);
  }

  @Override
  public void activitySampleFriendBackingViewHolderSeeActivityClicked(final @NonNull ActivitySampleFriendBackingViewHolder viewHolder) {
    seeActivityClicked();
  }

  @Override
  public void activitySampleFriendFollowViewHolderSeeActivityClicked(final @NonNull ActivitySampleFriendFollowViewHolder viewHolder) {
    seeActivityClicked();
  }

  @Override
  public void activitySampleProjectViewHolderSeeActivityClicked(final @NonNull ActivitySampleProjectViewHolder viewHolder) {
    seeActivityClicked();
  }

  // OUTPUTS
  private final BehaviorSubject<List<Project>> projects = BehaviorSubject.create();
  @Override
  public Observable<List<Project>> projects() {
    return projects;
  }

  private final BehaviorSubject<DiscoveryParams> selectedParams = BehaviorSubject.create();

  @Override
  public Observable<DiscoveryParams> selectedParams() {
    return selectedParams;
  }
  private final BehaviorSubject<Activity> activity = BehaviorSubject.create();
  public Observable<Activity> activity() {
    return activity;
  }

  private final BehaviorSubject<Boolean> shouldShowOnboarding = BehaviorSubject.create();
  @Override
  public Observable<Boolean> shouldShowOnboarding() {
    return shouldShowOnboarding;
  }

  private final PublishSubject<Void> showInternalTools = PublishSubject.create();
  @Override
  public Observable<Void> showInternalTools() {
    return showInternalTools;
  }

  private final PublishSubject<Void> showProfile = PublishSubject.create();
  @Override
  public Observable<Void> showProfile() {
    return showProfile;
  }

  private final PublishSubject<Pair<Project, RefTag>> showProject = PublishSubject.create();
  @Override
  public Observable<Pair<Project, RefTag>> showProject() {
    return showProject;
  }

  private final PublishSubject<Activity> showActivityUpdate = PublishSubject.create();
  @Override
  public Observable<Activity> showActivityUpdate() {
    return showActivityUpdate;
  }

  private final PublishSubject<Void> showActivityFeed = PublishSubject.create();
  @Override
  public Observable<Void> showActivityFeed() {
    return showActivityFeed;
  }

  private final PublishSubject<Void> showLoginTout = PublishSubject.create();
  @Override
  public Observable<Void> showLoginTout() {
    return showLoginTout;
  }

  private final PublishSubject<Void> showSettings = PublishSubject.create();
  @Override
  public Observable<Void> showSettings() {
    return showSettings;
  }

  private BehaviorSubject<NavigationDrawerData> navigationDrawerData = BehaviorSubject.create();
  @Override
  public Observable<NavigationDrawerData> navigationDrawerData() {
    return navigationDrawerData;
  }

  private BehaviorSubject<Boolean> drawerIsOpen = BehaviorSubject.create(false);
  @Override
  public Observable<Boolean> drawerIsOpen() {
    return drawerIsOpen;
  }

  private boolean hasSeenOnboarding = false;

  public final DiscoveryViewModelInputs inputs = this;
  public final DiscoveryViewModelOutputs outputs = this;

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    buildCheck.bind(this, webClient);

    final Observable<List<Category>> categories = apiClient.fetchCategories()
      .compose(Transformers.neverError())
      .flatMap(Observable::from)
      .toSortedList()
      .share();

    final Observable<List<Category>> rootCategories = categories
      .flatMap(Observable::from)
      .filter(Category::isRoot)
      .toList();

    final Observable<Category> clickedCategory = parentFilterRowClick
      .map(NavigationDrawerData.Section.Row::params)
      .map(DiscoveryParams::category);

    final PublishSubject<Category> expandedParams = PublishSubject.create();

    final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
      ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
        .nextPage(nextPage)
        .startOverWith(selectedParams)
        .envelopeToListOfData(DiscoverEnvelope::projects)
        .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
        .loadWithParams(apiClient::fetchProjects)
        .loadWithPaginationPath(apiClient::fetchProjects)
        .clearWhenStartingOver(true)
        .concater(ListUtils::concatDistinct)
        .build();

    paginator.paginatedData
      .compose(Transformers.combineLatestPair(rootCategories))
      .map(pc -> DiscoveryUtils.fillRootCategoryForFeaturedProjects(pc.first, pc.second))
      .compose(bindToLifecycle())
      .subscribe(projects::onNext);

    selectedParams.compose(Transformers.takePairWhen(paginator.loadingPage))
      .map(paramsAndPage -> paramsAndPage.first.toBuilder().page(paramsAndPage.second).build())
      .compose(bindToLifecycle())
      .subscribe(p -> koala.trackDiscovery(p, !hasSeenOnboarding));

    selectedParams
      .compose(Transformers.combineLatestPair(currentUser.isLoggedIn()))
      .map(pu -> isOnboardingVisible(pu.first, pu.second))
      .doOnNext(show -> hasSeenOnboarding = show || hasSeenOnboarding)
      .compose(bindToLifecycle())
      .subscribe(shouldShowOnboarding::onNext);

    currentUser.loggedInUser()
      .compose(Transformers.combineLatestPair(selectedParams))
      .flatMap(__ -> this.fetchActivity())
      .filter(this::activityHasNotBeenSeen)
      .doOnNext(this::saveLastSeenActivityId)
      .compose(bindToLifecycle())
      .subscribe(activity::onNext);

    // Clear activity sample when params change
    selectedParams
      .map(__ -> (Activity) null)
      .compose(bindToLifecycle())
      .subscribe(activity::onNext);

    Observable.combineLatest(
      categories,
      selectedParams,
      expandedParams,
      currentUser.observable(),
      DiscoveryDrawerUtils::deriveNavigationDrawerData
    )
      .compose(bindToLifecycle())
      .subscribe(navigationDrawerData::onNext);
    
    selectedParams
      .compose(Transformers.takePairWhen(clickProject))
      .map(pp -> DiscoveryViewModel.projectAndRefTagFromParamsAndProject(pp.first, pp.second))
      .compose(bindToLifecycle())
      .subscribe(showProject::onNext);

    clickActivityProject
      .map(p -> Pair.create(p, RefTag.activitySample()))
      .compose(bindToLifecycle())
      .subscribe(showProject::onNext);

    childFilterRowClick
      .mergeWith(topFilterRowClick)
      .map(__ -> false)
      .compose(bindToLifecycle())
      .subscribe(drawerIsOpen::onNext);

    openDrawer
      .compose(bindToLifecycle())
      .subscribe(drawerIsOpen::onNext);

    final Observable<DiscoveryParams> paramsClicked = childFilterRowClick
        .mergeWith(topFilterRowClick)
        .map(NavigationDrawerData.Section.Row::params);

    paramsClicked
      .compose(bindToLifecycle())
      .subscribe(selectedParams::onNext);

    topFilterRowClick
      .compose(bindToLifecycle())
      .subscribe(__ -> expandedParams.onNext(null));

    navigationDrawerData
      .map(NavigationDrawerData::expandedCategory)
      .compose(Transformers.takePairWhen(clickedCategory))
      .map(expandedAndClickedCategory -> toggleExpandedCategory(expandedAndClickedCategory.first, expandedAndClickedCategory.second))
      .compose(bindToLifecycle())
      .subscribe(expandedParams::onNext);

    internalToolsClick
      .compose(bindToLifecycle())
      .subscribe(__ -> showInternalTools.onNext(null));
    profileClick
      .compose(bindToLifecycle())
      .subscribe(__ -> showProfile.onNext(null));
    settingsClick
      .compose(bindToLifecycle())
      .subscribe(__ -> showSettings.onNext(null));

    discoveryOnboardingLoginToutClick
      .mergeWith(loggedOutLoginToutClick)
      .compose(bindToLifecycle())
      .subscribe(__ -> showLoginTout.onNext(null));

    // Closing the drawer while starting an activity is a little overwhelming,
    // so put the close on a delay so it happens out of sight.
    profileClick
      .mergeWith(internalToolsClick)
      .mergeWith(settingsClick)
      .mergeWith(discoveryOnboardingLoginToutClick)
      .mergeWith(loggedOutLoginToutClick)
      .delay(1, TimeUnit.SECONDS)
      .map(__ -> false)
      .compose(bindToLifecycle())
      .subscribe(drawerIsOpen::onNext);

    paramsClicked
      .compose(bindToLifecycle())
      .subscribe(koala::trackDiscoveryFilterSelected);

    openDrawer
      .filter(BooleanUtils::isTrue)
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackDiscoveryFilters());

    expandedParams.onNext(null);

    intent
      .flatMap(i -> DiscoveryIntentMapper.params(i, apiClient))
      .compose(bindToLifecycle())
      .subscribe(selectedParams::onNext);

    // Seed selected params when we are freshly launching the app with no data.
    intent
      .take(1)
      .map(Intent::getAction)
      .filter(Intent.ACTION_MAIN::equals)
      .map(__ -> DiscoveryParams.builder().staffPicks(true).build())
      .compose(bindToLifecycle())
      .subscribe(selectedParams::onNext);

    // TODO: Merge with action main
    activityResult
      .filter(DiscoveryViewModel::isSuccessfulLogin)
      .map(__ -> DiscoveryParams.builder().staffPicks(true).build())
      .compose(bindToLifecycle())
      .subscribe(selectedParams::onNext);
  }

  private boolean isOnboardingVisible(final @NonNull DiscoveryParams currentParams, final boolean isLoggedIn) {
    return !isLoggedIn && !hasSeenOnboarding && isTrue(currentParams.staffPicks());
  }

  /**
   * Converts a pair (params, project) into a (project, refTag) pair that does some extra logic around POTD and
   * featured projects..
   */
  private static @NonNull Pair<Project, RefTag> projectAndRefTagFromParamsAndProject(final @NonNull DiscoveryParams params, final @NonNull Project project) {
    final RefTag refTag;
    if (project.isPotdToday()) {
      refTag = RefTag.discoverPotd();
    } else if (project.isFeaturedToday()) {
      refTag = RefTag.categoryFeatured();
    } else {
      refTag = DiscoveryParamsUtils.refTag(params);
    }

    return new Pair<>(project, refTag);
  }

  public Observable<Activity> fetchActivity() {
    return apiClient.fetchActivities(1)
      .map(ActivityEnvelope::activities)
      .map(ListUtils::first)
      .filter(ObjectUtils::isNotNull)
      .compose(Transformers.neverError());
  }

  private boolean activityHasNotBeenSeen(final @Nullable Activity activity) {
    return activity != null && activity.id() != activitySamplePreference.get();
  }

  private static boolean isSuccessfulLogin(final @NonNull ActivityResult activityResult) {
    return activityResult.isOk() && activityResult.isRequestCode(ActivityRequestCodes.LOGIN_FLOW);
  }

  private static @Nullable Category toggleExpandedCategory(final @Nullable Category expandedCategory, final @NonNull Category clickedCategory) {
    if (expandedCategory != null && clickedCategory.id() == expandedCategory.id()) {
      return null;
    }
    return clickedCategory;
  }

  private void saveLastSeenActivityId(final @Nullable Activity activity) {
    if (activity != null) {
      activitySamplePreference.set((int) activity.id());
    }
  }

  private void seeActivityClicked() {
    showActivityFeed.onNext(null);
  }

  private void activitySampleProjectClicked(final @NonNull Project project) {
    clickActivityProject.onNext(project);
  }
}
