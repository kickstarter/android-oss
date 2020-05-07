package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ExperimentsClientType;
import com.kickstarter.libs.FragmentViewModel;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.models.OptimizelyFeature;
import com.kickstarter.libs.preferences.IntPreferenceType;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.libs.utils.ExperimentData;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.RefTagUtils;
import com.kickstarter.libs.utils.UserUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.adapters.DiscoveryAdapter;
import com.kickstarter.ui.data.Editorial;
import com.kickstarter.ui.fragments.DiscoveryFragment;
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public interface DiscoveryFragmentViewModel {

  interface Inputs extends DiscoveryAdapter.Delegate {
    /** Call when the page content should be cleared.  */
    void clearPage();

    /** Call when user clicks hearts to start animation.  */
    void heartContainerClicked();

    /** Call for project pagination. */
    void nextPage();

    /** Call when params from Discovery Activity change. */
    void paramsFromActivity(final DiscoveryParams params);

    /** Call when the projects should be refreshed. */
    void refresh();

    /**  Call when we should load the root categories. */
    void rootCategories(final List<Category> rootCategories);
  }

  interface Outputs {
    /**  Emits an activity for the activity sample view. */
    Observable<Activity> activity();

    /** Emits a boolean indicating whether projects are being fetched from the API. */
    Observable<Boolean> isFetchingProjects();

    /** Emits a list of projects to display.*/
    Observable<List<Pair<Project, DiscoveryParams>>> projectList();

    /** Emits a boolean that determines if an editorial should be shown. */
    Observable<Editorial> shouldShowEditorial();

    /** Emits a boolean that determines if the saved empty view should be shown. */
    Observable<Boolean> shouldShowEmptySavedView();

    /** Emits a boolean that determines if the onboarding view should be shown. */
    Observable<Boolean> shouldShowOnboardingView();

    /** Emits when the activity feed should be shown. */
    Observable<Boolean> showActivityFeed();

    /** Emits when the login tout activity should be shown. */
    Observable<Boolean> showLoginTout();

    /** Emits when the heart animation should play. */
    Observable<Void> startHeartAnimation();

    /** Emits an Editorial when we should start the {@link com.kickstarter.ui.activities.EditorialActivity}. */
    Observable<Editorial> startEditorialActivity();

    /** Emits a Project and RefTag pair when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Pair<Project, RefTag>> startProjectActivity();

    /** Emits an activity when we should start the {@link com.kickstarter.ui.activities.UpdateActivity}. */
    Observable<Activity> startUpdateActivity();
  }

  final class ViewModel extends FragmentViewModel<DiscoveryFragment> implements Inputs, Outputs {
    private final ApiClientType apiClient;
    private final CurrentUserType currentUser;
    private final IntPreferenceType activitySamplePreference;
    private final ExperimentsClientType optimizely;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.apiClient = environment.apiClient();
      this.activitySamplePreference = environment.activitySamplePreference();
      this.currentUser = environment.currentUser();
      this.optimizely = environment.optimizely();

      final Observable<User> changedUser = this.currentUser.observable()
        .distinctUntilChanged((u1, u2) -> !UserUtils.userHasChanged(u1, u2));

      final Observable<Boolean> userIsLoggedIn = this.currentUser.isLoggedIn()
        .distinctUntilChanged();

      final Observable<DiscoveryParams> selectedParams = Observable.combineLatest(
        changedUser,
        this.paramsFromActivity.distinctUntilChanged(),
        (__, params) -> params
      );

      final Observable<DiscoveryParams> startOverWith = Observable.merge(
        selectedParams,
        selectedParams.compose(takeWhen(this.refresh))
      );

      final ApiPaginator<Project, DiscoverEnvelope, DiscoveryParams> paginator =
        ApiPaginator.<Project, DiscoverEnvelope, DiscoveryParams>builder()
          .nextPage(this.nextPage)
          .startOverWith(startOverWith)
          .envelopeToListOfData(DiscoverEnvelope::projects)
          .envelopeToMoreUrl(env -> env.urls().api().moreProjects())
          .loadWithParams(this.apiClient::fetchProjects)
          .loadWithPaginationPath(this.apiClient::fetchProjects)
          .clearWhenStartingOver(false)
          .concater(ListUtils::concatDistinct)
          .build();

      paginator.isFetching()
        .compose(bindToLifecycle())
        .subscribe(this.isFetchingProjects);

      this.projectList
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(__ -> this.isFetchingProjects.onNext(false));

      final Observable<Pair<Project, RefTag>> activitySampleProjectClick = this.activitySampleProjectClick
        .map(p -> Pair.create(p, RefTag.activitySample()));

      final Observable<Pair<Project, RefTag>> projectCardClick = this.paramsFromActivity
        .compose(takePairWhen(this.projectCardClicked))
        .map(pp -> RefTagUtils.projectAndRefTagFromParamsAndProject(pp.first, pp.second));

      final Observable<List<Project>> projects = Observable.combineLatest(
        paginator.paginatedData(),
        this.rootCategories,
        DiscoveryUtils::fillRootCategoryForFeaturedProjects);

      Observable.combineLatest(projects,
        selectedParams.distinctUntilChanged(),
        ProjectUtils::combineProjectsAndParams)
        .compose(bindToLifecycle())
        .subscribe(this.projectList);

      this.showActivityFeed = this.activityClick;
      this.startUpdateActivity = this.activityUpdateClick;
      this.showLoginTout = this.discoveryOnboardingLoginToutClick;

      this.startProjectActivity = Observable.merge(
        activitySampleProjectClick,
        projectCardClick
      );

      this.clearPage
        .compose(bindToLifecycle())
        .subscribe(__ -> {
          this.shouldShowOnboardingView.onNext(false);
          this.activity.onNext(null);
          this.projectList.onNext(Collections.emptyList());
        });

      final Observable<User> userWhenOptimizelyReady = Observable.merge(
        changedUser,
        changedUser.compose(takeWhen(this.optimizelyReady))
      );

      final Observable<Boolean> lightsOnEnabled = userWhenOptimizelyReady
        .map(user -> this.optimizely.isFeatureEnabled(OptimizelyFeature.Key.LIGHTS_ON, new ExperimentData(user, null, null)))
        .distinctUntilChanged();

      this.currentUser.observable()
        .compose(combineLatestPair(this.paramsFromActivity))
        .compose(combineLatestPair(lightsOnEnabled))
        .map(defaultParamsAndEnabled -> isDefaultParams(defaultParamsAndEnabled.first) && BooleanUtils.isTrue(defaultParamsAndEnabled.second))
        .map(shouldShow -> shouldShow ? Editorial.LIGHTS_ON : null)
        .compose(bindToLifecycle())
        .subscribe(this.shouldShowEditorial);

      this.editorialClicked
        .compose(bindToLifecycle())
        .subscribe(this.startEditorialActivity);

      this.editorialClicked
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackEditorialCardClicked);

      this.paramsFromActivity
        .compose(combineLatestPair(userIsLoggedIn))
        .map(pu -> isOnboardingVisible(pu.first, pu.second))
        .compose(bindToLifecycle())
        .subscribe(this.shouldShowOnboardingView);

      this.paramsFromActivity
        .map(this::isSavedVisible)
        .compose(combineLatestPair(this.projectList))
        .map(savedAndProjects -> savedAndProjects.first && savedAndProjects.second.isEmpty())
        .compose(bindToLifecycle())
        .distinctUntilChanged()
        .subscribe(this.shouldShowEmptySavedView);

      this.shouldShowEmptySavedView
        .filter(BooleanUtils::isTrue)
        .map(__ -> null)
        .mergeWith(this.heartContainerClicked)
        .subscribe(__ -> this.startHeartAnimation.onNext(null));

      final Observable<Pair<User, DiscoveryParams>> loggedInUserAndParams = this.currentUser.loggedInUser()
        .distinctUntilChanged((u1, u2) -> !UserUtils.userHasChanged(u1, u2))
        .compose(combineLatestPair(this.paramsFromActivity));

      // Activity should show on the user's default params
      loggedInUserAndParams
        .filter(this::isDefaultParams)
        .flatMap(__ -> this.fetchActivity())
        .filter(this::activityHasNotBeenSeen)
        .doOnNext(this::saveLastSeenActivityId)
        .compose(bindToLifecycle())
        .subscribe(this.activity);

      // Clear activity sample when params change from default
      loggedInUserAndParams
        .filter(userAndParams -> !isDefaultParams(userAndParams))
        .map(__ -> (Activity) null)
        .compose(bindToLifecycle())
        .subscribe(this.activity);

      this.paramsFromActivity
        .compose(combineLatestPair(paginator.loadingPage().distinctUntilChanged()))
        .map(paramsAndPage -> paramsAndPage.first.toBuilder().page(paramsAndPage.second).build())
        .compose(combineLatestPair(userIsLoggedIn))
        .compose(bindToLifecycle())
        .subscribe(paramsAndLoggedIn -> {
          this.koala.trackDiscovery(
            paramsAndLoggedIn.first,
            isOnboardingVisible(paramsAndLoggedIn.first, paramsAndLoggedIn.second)
          );
        });

      this.paramsFromActivity
        .compose(combineLatestPair(paginator.loadingPage().distinctUntilChanged()))
        .filter(paramsAndPage -> paramsAndPage.second == 1)
        .compose(bindToLifecycle())
        .subscribe(paramsAndLoggedIn -> this.lake.trackExplorePageViewed(paramsAndLoggedIn.first));

      this.startUpdateActivity
        .map(Activity::project)
        .filter(ObjectUtils::isNotNull)
        .compose(bindToLifecycle())
        .subscribe(p -> this.koala.trackViewedUpdate(p, KoalaContext.Update.ACTIVITY_SAMPLE));

      this.refresh
        .compose(bindToLifecycle())
        .subscribe(v -> this.koala.trackDiscoveryRefreshTriggered());

      this.discoveryOnboardingLoginToutClick
        .compose(bindToLifecycle())
        .subscribe(v -> this.lake.trackLogInSignUpButtonClicked());
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

    private boolean isDefaultParams(final @NonNull Pair<User, DiscoveryParams> userAndParams) {
      final DiscoveryParams discoveryParams = userAndParams.second;
      final User user = userAndParams.first;
      return discoveryParams.equals(DiscoveryParams.getDefaultParams(user));
    }

    private boolean isOnboardingVisible(final @NonNull DiscoveryParams params, final boolean isLoggedIn) {
      final DiscoveryParams.Sort sort = params.sort();
      final boolean isSortHome = DiscoveryParams.Sort.MAGIC.equals(sort);
      return isTrue(params.isAllProjects()) && isSortHome && !isLoggedIn;
    }

    private boolean isSavedVisible(final @NonNull DiscoveryParams params) {
      return params.isSavedProjects();
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
    private final PublishSubject<Boolean> discoveryOnboardingLoginToutClick = PublishSubject.create();
    private final PublishSubject<Editorial> editorialClicked = PublishSubject.create();
    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<DiscoveryParams> paramsFromActivity = PublishSubject.create();
    private final PublishSubject<Project> projectCardClicked = PublishSubject.create();
    private final PublishSubject<Void> refresh = PublishSubject.create();
    private final PublishSubject<List<Category>> rootCategories = PublishSubject.create();

    private final BehaviorSubject<Activity> activity = BehaviorSubject.create();
    private final BehaviorSubject<Void> heartContainerClicked = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> isFetchingProjects = BehaviorSubject.create();
    private final BehaviorSubject<List<Pair<Project, DiscoveryParams>>> projectList = BehaviorSubject.create();
    private final Observable<Boolean> showActivityFeed;
    private final Observable<Boolean> showLoginTout;
    private final BehaviorSubject<Editorial> shouldShowEditorial = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> shouldShowEmptySavedView = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> shouldShowOnboardingView = BehaviorSubject.create();
    private final PublishSubject<Editorial> startEditorialActivity = PublishSubject.create();
    private final Observable<Pair<Project, RefTag>> startProjectActivity;
    private final Observable<Activity> startUpdateActivity;
    private final BehaviorSubject<Void> startHeartAnimation = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

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
    @Override public void editorialViewHolderClicked(final @NonNull Editorial editorial) {
      this.editorialClicked.onNext(editorial);
    }
    @Override public void projectCardViewHolderClicked(final @NonNull Project project) {
      this.projectCardClicked.onNext(project);
    }
    @Override public void refresh() {
      this.refresh.onNext(null);
    }
    @Override public void rootCategories(final @NonNull List<Category> rootCategories) {
      this.rootCategories.onNext(rootCategories);
    }
    @Override public void clearPage() {
      this.clearPage.onNext(null);
    }
    @Override public void heartContainerClicked() {
      this.heartContainerClicked.onNext(null);
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

    @Override public @NonNull Observable<Activity> activity() {
      return this.activity;
    }
    @Override public @NonNull Observable<Boolean> isFetchingProjects() {
      return this.isFetchingProjects;
    }
    @Override public @NonNull Observable<List<Pair<Project, DiscoveryParams>>> projectList() {
      return this.projectList;
    }
    @Override public @NonNull Observable<Boolean> showActivityFeed() {
      return this.showActivityFeed;
    }
    @Override public @NonNull Observable<Boolean> showLoginTout() {
      return this.showLoginTout;
    }
    @Override public @NonNull Observable<Editorial> shouldShowEditorial() {
      return this.shouldShowEditorial;
    }
    @Override public @NonNull Observable<Boolean> shouldShowEmptySavedView() {
      return this.shouldShowEmptySavedView;
    }
    @Override public @NonNull Observable<Void> startHeartAnimation() {
      return this.startHeartAnimation;
    }
    @Override public @NonNull Observable<Editorial> startEditorialActivity() {
      return this.startEditorialActivity;
    }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<Boolean> shouldShowOnboardingView() {
      return this.shouldShowOnboardingView;
    }
    @Override public @NonNull Observable<Activity> startUpdateActivity() {
      return this.startUpdateActivity;
    }
  }
}
