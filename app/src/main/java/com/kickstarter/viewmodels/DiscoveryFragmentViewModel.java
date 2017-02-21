package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.FragmentViewModel;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.preferences.IntPreferenceType;
import com.kickstarter.libs.utils.DiscoveryParamsUtils;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.fragments.DiscoveryFragment;
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;
import com.kickstarter.ui.viewholders.ProjectCardViewHolder;
import com.kickstarter.viewmodels.outputs.DiscoveryFragmentViewModelInputs;
import com.kickstarter.viewmodels.outputs.DiscoveryFragmentViewModelOutputs;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public final class DiscoveryFragmentViewModel extends FragmentViewModel<DiscoveryFragment> implements
  DiscoveryFragmentViewModelInputs, DiscoveryFragmentViewModelOutputs {
  private final ApiClientType apiClient;
  private final CurrentUserType currentUser;
  private final IntPreferenceType activitySamplePreference;

  public DiscoveryFragmentViewModel(final @NonNull Environment environment) {
    super(environment);

    apiClient = environment.apiClient();
    activitySamplePreference = environment.activitySamplePreference();
    currentUser = environment.currentUser();

    final Observable<DiscoveryParams> selectedParams = Observable.combineLatest(
      currentUser.observable(),
      paramsFromActivity.distinctUntilChanged(),
      (__, params) -> params
    );

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

    final Observable<Pair<Project, RefTag>> projectCardClick = paramsFromActivity
      .compose(takePairWhen(clickProject))
      .map(pp -> DiscoveryFragmentViewModel.projectAndRefTagFromParamsAndProject(pp.first, pp.second));

    final Observable<Pair<Project, RefTag>> activitySampleProjectClick = this.activitySampleProjectClick
      .map(p -> Pair.create(p, RefTag.activitySample()));

    Observable.combineLatest(
      paginator.paginatedData(),
      rootCategories,
      DiscoveryUtils::fillRootCategoryForFeaturedProjects
    )
      .compose(bindToLifecycle())
      .subscribe(projects);

    showActivityFeed = activityClick;
    showActivityUpdate = activityUpdateClick;
    showLoginTout = discoveryOnboardingLoginToutClick;

    Observable.merge(
      projectCardClick,
      activitySampleProjectClick
    )
      .compose(bindToLifecycle())
      .subscribe(showProject);

    clearPage
      .compose(bindToLifecycle())
      .subscribe(__ -> {
        shouldShowOnboardingView.onNext(false);
        activity.onNext(null);
        projects.onNext(new ArrayList<>());
      });

    paramsFromActivity
      .compose(combineLatestPair(currentUser.isLoggedIn()))
      .map(pu -> isOnboardingVisible(pu.first, pu.second))
      .compose(bindToLifecycle())
      .subscribe(shouldShowOnboardingView);

    currentUser.loggedInUser()
      .compose(combineLatestPair(paramsFromActivity))
      .flatMap(__ -> this.fetchActivity())
      .filter(this::activityHasNotBeenSeen)
      .doOnNext(this::saveLastSeenActivityId)
      .compose(bindToLifecycle())
      .subscribe(activity);

    // Clear activity sample when params change
    paramsFromActivity
      .map(__ -> (Activity) null)
      .compose(bindToLifecycle())
      .subscribe(activity);

    paramsFromActivity
      .compose(combineLatestPair(paginator.loadingPage().distinctUntilChanged()))
      .map(paramsAndPage -> paramsAndPage.first.toBuilder().page(paramsAndPage.second).build())
      .compose(combineLatestPair(currentUser.isLoggedIn()))
      .compose(bindToLifecycle())
      .subscribe(paramsAndLoggedIn -> {
        koala.trackDiscovery(
          paramsAndLoggedIn.first,
          isOnboardingVisible(paramsAndLoggedIn.first, paramsAndLoggedIn.second)
        );
      });

    showActivityUpdate
      .map(Activity::project)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(p -> koala.trackViewedUpdate(p, KoalaContext.Update.ACTIVITY_SAMPLE));
  }

  private boolean activityHasNotBeenSeen(final @Nullable Activity activity) {
    return activity != null && activity.id() != activitySamplePreference.get();
  }

  private Observable<Activity> fetchActivity() {
    return apiClient.fetchActivities(1)
      .map(ActivityEnvelope::activities)
      .map(ListUtils::first)
      .filter(ObjectUtils::isNotNull)
      .compose(neverError());
  }

  private boolean isOnboardingVisible(final @NonNull DiscoveryParams params, final boolean isLoggedIn) {
    final DiscoveryParams.Sort sort = params.sort();
    final boolean isSortHome = DiscoveryParams.Sort.HOME.equals(sort);
    return isTrue(params.isAllProjects()) && isSortHome && !isLoggedIn;
  }

  /**
   * Converts a pair (params, project) into a (project, refTag) pair that does some extra logic around POTD and
   * featured projects..
   */
  private static @NonNull Pair<Project, RefTag> projectAndRefTagFromParamsAndProject(final @NonNull DiscoveryParams params,
    final @NonNull Project project) {
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

  private void saveLastSeenActivityId(final @Nullable Activity activity) {
    if (activity != null) {
      activitySamplePreference.set((int) activity.id());
    }
  }

  private final PublishSubject<Boolean> activityClick = PublishSubject.create();
  private final PublishSubject<Project> activitySampleProjectClick = PublishSubject.create();
  private final PublishSubject<Activity> activityUpdateClick = PublishSubject.create();
  private final PublishSubject<Void> clearPage = PublishSubject.create();
  private final PublishSubject<Project> clickProject = PublishSubject.create();
  private final PublishSubject<Boolean> discoveryOnboardingLoginToutClick = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  private final PublishSubject<DiscoveryParams> paramsFromActivity = PublishSubject.create();
  private final PublishSubject<List<Category>> rootCategories = PublishSubject.create();

  private final BehaviorSubject<Activity> activity = BehaviorSubject.create();
  private final BehaviorSubject<List<Project>> projects = BehaviorSubject.create();
  private final Observable<Boolean> showActivityFeed;
  private final Observable<Activity> showActivityUpdate;
  private final Observable<Boolean> showLoginTout;
  private final PublishSubject<Pair<Project, RefTag>> showProject = PublishSubject.create();
  private final BehaviorSubject<Boolean> shouldShowOnboardingView = BehaviorSubject.create();

  public final DiscoveryFragmentViewModelInputs inputs = this;
  public final DiscoveryFragmentViewModelOutputs outputs = this;

  @Override public void activitySampleFriendBackingViewHolderProjectClicked(final @NonNull ActivitySampleFriendBackingViewHolder viewHolder,
    final @NonNull Project project) {
    activitySampleProjectClick.onNext(project);
  }
  @Override public void activitySampleFriendBackingViewHolderSeeActivityClicked(final @NonNull ActivitySampleFriendBackingViewHolder viewHolder) {
    activityClick.onNext(true);
  }
  @Override public void activitySampleFriendFollowViewHolderSeeActivityClicked(final @NonNull ActivitySampleFriendFollowViewHolder viewHolder) {
    activityClick.onNext(true);
  }
  @Override public void activitySampleProjectViewHolderProjectClicked(final @NonNull ActivitySampleProjectViewHolder viewHolder,
    final @NonNull Project project) {
    activitySampleProjectClick.onNext(project);
  }
  @Override public void activitySampleProjectViewHolderSeeActivityClicked(final @NonNull ActivitySampleProjectViewHolder viewHolder) {
    activityClick.onNext(true);
  }
  @Override public void activitySampleProjectViewHolderUpdateClicked(final @NonNull ActivitySampleProjectViewHolder viewHolder,
    final @NonNull Activity activity) {
    activityUpdateClick.onNext(activity);
  }
  @Override public void rootCategories(final @NonNull List<Category> rootCategories) {
    this.rootCategories.onNext(rootCategories);
  }
  @Override public void clearPage() {
    clearPage.onNext(null);
  }
  @Override public void discoveryOnboardingViewHolderLoginToutClick(final @NonNull DiscoveryOnboardingViewHolder viewHolder) {
    discoveryOnboardingLoginToutClick.onNext(true);
  }
  @Override public void nextPage() {
    nextPage.onNext(null);
  }
  @Override public void paramsFromActivity(final @NonNull DiscoveryParams params) {
    paramsFromActivity.onNext(params);
  }
  @Override public void projectCardViewHolderClick(final @NonNull ProjectCardViewHolder viewHolder, final @NonNull Project project) {
    clickProject.onNext(project);
  }

  @Override public @NonNull Observable<Activity> activity() {
    return activity;
  }
  @Override public @NonNull Observable<List<Project>> projects() {
    return projects;
  }
  @Override public @NonNull Observable<Boolean> showActivityFeed() {
    return showActivityFeed;
  }
  @Override public @NonNull Observable<Activity> showActivityUpdate() {
    return showActivityUpdate;
  }
  @Override public @NonNull Observable<Boolean> showLoginTout() {
    return showLoginTout;
  }
  @Override public @NonNull Observable<Pair<Project, RefTag>> showProject() {
    return showProject;
  }
  @Override public @NonNull Observable<Boolean> shouldShowOnboardingView() {
    return shouldShowOnboardingView;
  }
}
