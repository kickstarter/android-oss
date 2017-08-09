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

    this.apiClient = environment.apiClient();
    this.activitySamplePreference = environment.activitySamplePreference();
    this.currentUser = environment.currentUser();

    final Observable<DiscoveryParams> selectedParams = Observable.combineLatest(
      this.currentUser.observable(),
      this.paramsFromActivity.distinctUntilChanged(),
      (__, params) -> params
    );

    final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
      ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
        .nextPage(this.nextPage)
        .startOverWith(selectedParams)
        .envelopeToListOfData(DiscoverEnvelope::projects)
        .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
        .loadWithParams(this.apiClient::fetchProjects)
        .loadWithPaginationPath(this.apiClient::fetchProjects)
        .clearWhenStartingOver(true)
        .concater(ListUtils::concatDistinct)
        .build();

    final Observable<Pair<Project, RefTag>> projectCardClick = this.paramsFromActivity
      .compose(takePairWhen(this.clickProject))
      .map(pp -> DiscoveryFragmentViewModel.projectAndRefTagFromParamsAndProject(pp.first, pp.second));

    final Observable<Pair<Project, RefTag>> activitySampleProjectClick = this.activitySampleProjectClick
      .map(p -> Pair.create(p, RefTag.activitySample()));

    Observable.combineLatest(
      paginator.paginatedData(),
      this.rootCategories,
      DiscoveryUtils::fillRootCategoryForFeaturedProjects
    )
      .compose(bindToLifecycle())
      .subscribe(this.projectList);

    this.showActivityFeed = this.activityClick;
    this.showActivityUpdate = this.activityUpdateClick;
    this.showLoginTout = this.discoveryOnboardingLoginToutClick;

    Observable.merge(
      projectCardClick,
      activitySampleProjectClick
    )
      .compose(bindToLifecycle())
      .subscribe(this.showProject);

    this.clearPage
      .compose(bindToLifecycle())
      .subscribe(__ -> {
        this.shouldShowOnboardingView.onNext(false);
        this.activity.onNext(null);
        this.projectList.onNext(new ArrayList<>());
      });

    this.paramsFromActivity
      .compose(combineLatestPair(this.currentUser.isLoggedIn()))
      .map(pu -> isOnboardingVisible(pu.first, pu.second))
      .compose(bindToLifecycle())
      .subscribe(this.shouldShowOnboardingView);

    this.currentUser.loggedInUser()
      .compose(combineLatestPair(this.paramsFromActivity))
      .flatMap(__ -> this.fetchActivity())
      .filter(this::activityHasNotBeenSeen)
      .doOnNext(this::saveLastSeenActivityId)
      .compose(bindToLifecycle())
      .subscribe(this.activity);

    // Clear activity sample when params change
    this.paramsFromActivity
      .map(__ -> (Activity) null)
      .compose(bindToLifecycle())
      .subscribe(this.activity);

    this.paramsFromActivity
      .compose(combineLatestPair(paginator.loadingPage().distinctUntilChanged()))
      .map(paramsAndPage -> paramsAndPage.first.toBuilder().page(paramsAndPage.second).build())
      .compose(combineLatestPair(this.currentUser.isLoggedIn()))
      .compose(bindToLifecycle())
      .subscribe(paramsAndLoggedIn -> {
        this.koala.trackDiscovery(
          paramsAndLoggedIn.first,
          isOnboardingVisible(paramsAndLoggedIn.first, paramsAndLoggedIn.second)
        );
      });

    this.showActivityUpdate
      .map(Activity::project)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(p -> this.koala.trackViewedUpdate(p, KoalaContext.Update.ACTIVITY_SAMPLE));
  }

  private boolean activityHasNotBeenSeen(final @Nullable Activity activity) {
    return activity != null && activity.id() != this.activitySamplePreference.get();
  }

  private Observable<Activity> fetchActivity() {
    return this.apiClient.fetchActivities(1)
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
   * featured projectList..
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
      this.activitySamplePreference.set((int) activity.id());
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
  private final BehaviorSubject<List<Project>> projectList = BehaviorSubject.create();
  private final Observable<Boolean> showActivityFeed;
  private final Observable<Activity> showActivityUpdate;
  private final Observable<Boolean> showLoginTout;
  private final PublishSubject<Pair<Project, RefTag>> showProject = PublishSubject.create();
  private final BehaviorSubject<Boolean> shouldShowOnboardingView = BehaviorSubject.create();

  public final DiscoveryFragmentViewModelInputs inputs = this;
  public final DiscoveryFragmentViewModelOutputs outputs = this;

  @Override public void activitySampleFriendBackingViewHolderProjectClicked(final @NonNull ActivitySampleFriendBackingViewHolder viewHolder,
    final @NonNull Project project) {
    this.activitySampleProjectClick.onNext(project);
  }
  @Override public void activitySampleFriendBackingViewHolderSeeActivityClicked(final @NonNull ActivitySampleFriendBackingViewHolder viewHolder) {
    this.activityClick.onNext(true);
  }
  @Override public void activitySampleFriendFollowViewHolderSeeActivityClicked(final @NonNull ActivitySampleFriendFollowViewHolder viewHolder) {
    this.activityClick.onNext(true);
  }
  @Override public void activitySampleProjectViewHolderProjectClicked(final @NonNull ActivitySampleProjectViewHolder viewHolder,
    final @NonNull Project project) {
    this.activitySampleProjectClick.onNext(project);
  }
  @Override public void activitySampleProjectViewHolderSeeActivityClicked(final @NonNull ActivitySampleProjectViewHolder viewHolder) {
    this.activityClick.onNext(true);
  }
  @Override public void activitySampleProjectViewHolderUpdateClicked(final @NonNull ActivitySampleProjectViewHolder viewHolder,
    final @NonNull Activity activity) {
    this.activityUpdateClick.onNext(activity);
  }
  @Override public void rootCategories(final @NonNull List<Category> rootCategories) {
    this.rootCategories.onNext(rootCategories);
  }
  @Override public void clearPage() {
    this.clearPage.onNext(null);
  }
  @Override public void discoveryOnboardingViewHolderLoginToutClick(final @NonNull DiscoveryOnboardingViewHolder viewHolder) {
    this.discoveryOnboardingLoginToutClick.onNext(true);
  }
  @Override public void nextPage() {
    this.nextPage.onNext(null);
  }
  @Override public void paramsFromActivity(final @NonNull DiscoveryParams params) {
    this.paramsFromActivity.onNext(params);
  }
  @Override public void projectCardViewHolderClick(final @NonNull ProjectCardViewHolder viewHolder, final @NonNull Project project) {
    this.clickProject.onNext(project);
  }

  @Override public @NonNull Observable<Activity> activity() {
    return this.activity;
  }
  @Override public @NonNull Observable<List<Project>> projectList() {
    return this.projectList;
  }
  @Override public @NonNull Observable<Boolean> showActivityFeed() {
    return this.showActivityFeed;
  }
  @Override public @NonNull Observable<Activity> showActivityUpdate() {
    return this.showActivityUpdate;
  }
  @Override public @NonNull Observable<Boolean> showLoginTout() {
    return this.showLoginTout;
  }
  @Override public @NonNull Observable<Pair<Project, RefTag>> showProject() {
    return this.showProject;
  }
  @Override public @NonNull Observable<Boolean> shouldShowOnboardingView() {
    return this.shouldShowOnboardingView;
  }
}
