package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.DiscoveryDrawerUtils;
import com.kickstarter.libs.utils.DiscoveryParamsUtils;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.WebClient;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.ParentFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder;
import com.kickstarter.viewmodels.inputs.DiscoveryViewModelInputs;
import com.kickstarter.viewmodels.outputs.DiscoveryViewModelOutputs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.utils.BoolUtils.isTrue;

public final class DiscoveryViewModel extends ViewModel<DiscoveryActivity> implements DiscoveryViewModelInputs,
  DiscoveryViewModelOutputs {
  protected @Inject ApiClientType apiClient;
  protected @Inject WebClient webClient;
  protected @Inject BuildCheck buildCheck;
  protected @Inject CurrentUser currentUser;

  // INPUTS
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  @Override
  public void nextPage() {
    nextPage.onNext(null);
  }

  private final PublishSubject<Void> filterButtonClicked = PublishSubject.create();

  private final PublishSubject<DiscoveryParams> initializer = PublishSubject.create();
  @Override
  public void initializer(final @NonNull DiscoveryParams params) {
    initializer.onNext(params);
  }

  // ONBOARDING DELEGATE INPUTS
  @Override
  public void discoveryOnboardingViewHolderSignupLoginClick(DiscoveryOnboardingViewHolder viewHolder) {
    loginClick.onNext(null);
  }

  // PROJECT VIEW HOLDER DELEGATE INPUTS
  private final PublishSubject<Project> projectClicked = PublishSubject.create();
  @Override
  public void projectCardViewHolderClick(ProjectCardViewHolder viewHolder, Project project) {
    projectClicked.onNext(project);
  }

  // NAVIGATION DRAWER DELEGATE INPUTS
  private PublishSubject<NavigationDrawerData.Section.Row> childFilterRowClick = PublishSubject.create();
  @Override
  public void childFilterViewHolderRowClick(@NonNull ChildFilterViewHolder viewHolder, @NonNull NavigationDrawerData.Section.Row row) {
    childFilterRowClick.onNext(row);
  }

  private PublishSubject<Void> loginClick = PublishSubject.create();
  @Override
  public void loggedOutViewHolderLoginClick(final @NonNull LoggedOutViewHolder viewHolder) {
    loginClick.onNext(null);
  }

  private PublishSubject<NavigationDrawerData.Section.Row> parentFilterRowClick = PublishSubject.create();
  @Override
  public void parentFilterViewHolderRowClick(@NonNull ParentFilterViewHolder viewHolder, @NonNull NavigationDrawerData.Section.Row row) {
    parentFilterRowClick.onNext(row);
  }

  private PublishSubject<User> profileClick = PublishSubject.create();
  @Override
  public void loggedInViewHolderProfileClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user) {
    profileClick.onNext(user);
  }

  private PublishSubject<User> settingsClick = PublishSubject.create();
  @Override
  public void loggedInViewHolderSettingsClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user) {
    settingsClick.onNext(user);
  }

  private PublishSubject<NavigationDrawerData.Section.Row> topFilterRowClick = PublishSubject.create();
  @Override
  public void topFilterViewHolderRowClick(@NonNull TopFilterViewHolder viewHolder, @NonNull NavigationDrawerData.Section.Row row) {
    topFilterRowClick.onNext(row);
  }

  // OUTPUTS
  private final BehaviorSubject<List<Project>> projects = BehaviorSubject.create();
  @Override
  public Observable<List<Project>> projects() {
    return projects;
  }

  private final BehaviorSubject<DiscoveryParams> selectedParams = BehaviorSubject.create();

  @Override
  public Observable<DiscoveryParams> params() {
    return selectedParams;
  }

  private final BehaviorSubject<Boolean> shouldShowOnboarding = BehaviorSubject.create();
  @Override
  public Observable<Boolean> shouldShowOnboarding() {
    return shouldShowOnboarding;
  }

  private final PublishSubject<Void> showLogin = PublishSubject.create();
  @Override
  public Observable<Void> showLogin() {
    return showLogin;
  }

  @Override
  public Observable<DiscoveryParams> showFilters() {
    return selectedParams.compose(Transformers.takeWhen(filterButtonClicked));
  }

  private final PublishSubject<Void> showProfile = PublishSubject.create();
  @Override
  public Observable<Void> showProfile() {
    return showProfile;
  }

  @Override
  public Observable<Pair<Project, RefTag>> showProject() {
    return selectedParams.compose(Transformers.takePairWhen(projectClicked))
      .map(pp -> DiscoveryViewModel.projectAndRefTagFromParamsAndProject(pp.first, pp.second));
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

  private BehaviorSubject<Boolean> openDrawer = BehaviorSubject.create(false);
  @Override
  public Observable<Boolean> openDrawer() {
    return openDrawer;
  }

  private boolean hasSeenOnboarding = false;

  public final DiscoveryViewModelInputs inputs = this;
  public final DiscoveryViewModelOutputs outputs = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    buildCheck.bind(this, webClient);

    final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
      ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
        .nextPage(nextPage)
        .startOverWith(selectedParams)
        .envelopeToListOfData(DiscoverEnvelope::projects)
        .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
        .loadWithParams(apiClient::fetchProjects)
        .loadWithPaginationPath(apiClient::fetchProjects)
        .pageTransformation(this::bringPotdToFront)
        .clearWhenStartingOver(true)
        .concater(ListUtils::concatDistinct)
        .build();

    addSubscription(
      selectedParams.compose(Transformers.takePairWhen(paginator.loadingPage))
        .map(paramsAndPage -> paramsAndPage.first.toBuilder().page(paramsAndPage.second).build())
        .subscribe(p -> koala.trackDiscovery(p, !hasSeenOnboarding))
    );

    addSubscription(paginator.paginatedData.subscribe(projects));

    addSubscription(
      selectedParams
        .compose(Transformers.combineLatestPair(currentUser.isLoggedIn()))
        .map(pu -> isOnboardingVisible(pu.first, pu.second))
        .doOnNext(show -> hasSeenOnboarding = show || hasSeenOnboarding)
        .subscribe(shouldShowOnboarding::onNext)
    );

    initializer.subscribe(this.selectedParams::onNext);
    initializer.onNext(DiscoveryParams.builder().staffPicks(true).build());


    // NAVIGATION DRAWER ONCREATE
    final Observable<List<Category>> categories = apiClient.fetchCategories()
      .compose(Transformers.neverError())
      .flatMap(Observable::from)
      .toSortedList()
      .share();

    PublishSubject<Category> expandedParams = PublishSubject.create();

    addSubscription(
      Observable.combineLatest(
        categories,
        selectedParams,
        expandedParams,
        currentUser.observable(),
        DiscoveryDrawerUtils::deriveNavigationDrawerData)
        .subscribe(navigationDrawerData::onNext)
    );

    // selectedParams.onNext(DiscoveryParams.builder().staffPicks(true).build());
    expandedParams.onNext(null);

    final Observable<Category> clickedCategory = parentFilterRowClick
      .map(NavigationDrawerData.Section.Row::params)
      .map(DiscoveryParams::category);

    addSubscription(
      childFilterRowClick
        .mergeWith(topFilterRowClick)
        .subscribe(__ -> openDrawer.onNext(false))
    );

    addSubscription(
      childFilterRowClick
        .mergeWith(topFilterRowClick)
        .map(NavigationDrawerData.Section.Row::params)
        .subscribe(selectedParams::onNext)
    );

    addSubscription(
      topFilterRowClick
        .subscribe(__ -> expandedParams.onNext(null))
    );

    addSubscription(
      navigationDrawerData
        .map(NavigationDrawerData::expandedCategory)
        .compose(Transformers.takePairWhen(clickedCategory))
        .map(expandedAndClickedCategory -> toggleExpandedCategory(expandedAndClickedCategory.first, expandedAndClickedCategory.second))
        .subscribe(expandedParams::onNext)
    );

    addSubscription(
      loginClick
        .subscribe(__ -> showLogin.onNext(null))
    );

    addSubscription(
      profileClick
        .subscribe(__ -> showProfile.onNext(null))
    );

    addSubscription(
      settingsClick
        .subscribe(__ -> showSettings.onNext(null))
    );

    // Closing the drawer while starting an activity is a little overwhelming,
    // so put the close on a delay so it happens out of sight.
    addSubscription(
      profileClick
        .mergeWith(settingsClick)
        .mergeWith(loginClick.map(__ -> null))
        .delay(1, TimeUnit.SECONDS)
        .subscribe(__ -> {
          openDrawer.onNext(false);
        })
    );
  }

  /**
   * Given a list of projects, finds if it contains the POTD and if so
   * bumps it to the front of the list.
   */
  private List<Project> bringPotdToFront(final @NonNull List<Project> projects) {

    return Observable.from(projects)
      .reduce(new ArrayList<>(), this::prependPotdElseAppend)
      .toBlocking().single();
  }

  /**
   * Given a list of projects and a particular project, returns the list
   * when the project prepended if it's POTD and appends otherwise.
   */
  @NonNull private List<Project> prependPotdElseAppend(final @NonNull List<Project> projects, final @NonNull Project project) {
    return project.isPotdToday() ? ListUtils.prepend(projects, project) : ListUtils.append(projects, project);
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

  private static @Nullable Category toggleExpandedCategory(final @Nullable Category expandedCategory, final @NonNull Category clickedCategory) {
    if (expandedCategory != null && clickedCategory.id() == expandedCategory.id()) {
      return null;
    }
    return clickedCategory;
  }
}
