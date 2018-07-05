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
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.ObjectUtils;
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
import com.kickstarter.ui.fragments.DiscoveryFragment;
import com.kickstarter.ui.viewholders.ActivitySampleFriendBackingViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder;
import com.kickstarter.ui.viewholders.ActivitySampleProjectViewHolder;
import com.kickstarter.ui.viewholders.DiscoveryOnboardingViewHolder;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.utils.BooleanUtils.isTrue;

public interface DiscoveryFragmentViewModel {

  interface Inputs extends DiscoveryAdapter.Delegate {
    /** Call when params from Discovery Activity change. */
    void paramsFromActivity(final DiscoveryParams params);

    /** Call when the page content should be cleared.  */
    void clearPage();

    /** Call for project pagination. */
    void nextPage();

    /**  Call when we should load the root categories. */
    void rootCategories(final List<Category> rootCategories);
  }

  interface Outputs {
    /** . */
    Observable<Boolean> animateHearts();

    /** Emits a list of projects to display.*/
    Observable<List<Project>> projectList();

    /** Emits when the activity feed should be shown. */
    Observable<Boolean> showActivityFeed();

    /**  Emits an activity for the activity sample view. */
    Observable<Activity> activity();

    /** Emits when the login tout activity should be shown. */
    Observable<Boolean> showLoginTout();

    /** Emits a boolean that determines if the saved empty view should be shown. */
    Observable<Boolean> shouldShowEmptySavedView();

    /** Emits a boolean that determines if the onboarding view should be shown. */
    Observable<Boolean> shouldShowOnboardingView();

    /** Emits a Project and RefTag pair when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Pair<Project, RefTag>> startProjectActivity();

    /** Emits an activity when we should start the {@link com.kickstarter.ui.activities.UpdateActivity}. */
    Observable<Activity> startUpdateActivity();
  }

  final class ViewModel extends FragmentViewModel<DiscoveryFragment> implements Inputs, Outputs {
    private final ApiClientType apiClient;
    private final CurrentUserType currentUser;
    private final IntPreferenceType activitySamplePreference;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.apiClient = environment.apiClient();
      this.activitySamplePreference = environment.activitySamplePreference();
      this.currentUser = environment.currentUser();

      final Observable<User> changedUser = this.currentUser.observable()
        .distinctUntilChanged((u1, u2) -> !UserUtils.userHasChanged(u1, u2));

      final Observable<DiscoveryParams> selectedParams = Observable.combineLatest(
        changedUser,
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

      final Observable<Pair<Project, RefTag>> activitySampleProjectClick = this.activitySampleProjectClick
        .map(p -> Pair.create(p, RefTag.activitySample()));

      final Observable<Pair<Project, RefTag>> projectCardClick = this.paramsFromActivity
        .compose(takePairWhen(this.projectCardClicked))
        .map(pp -> RefTagUtils.projectAndRefTagFromParamsAndProject(pp.first, pp.second));

      Observable.combineLatest(
        paginator.paginatedData(),
        this.rootCategories,
        DiscoveryUtils::fillRootCategoryForFeaturedProjects
      )
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

      this.paramsFromActivity
        .compose(combineLatestPair(this.currentUser.isLoggedIn().distinctUntilChanged()))
        .map(pu -> isOnboardingVisible(pu.first, pu.second))
        .compose(bindToLifecycle())
        .subscribe(this.shouldShowOnboardingView);

      this.paramsFromActivity
        .compose(combineLatestPair(this.currentUser.isLoggedIn().distinctUntilChanged()))
        .map(pu -> isSavedVisible(pu.first, pu.second))
        .compose(takePairWhen(this.projectList))
        .map(savedVisibleAndProjects -> savedVisibleAndProjects.first && savedVisibleAndProjects.second.isEmpty())
        .compose(bindToLifecycle())
        .subscribe(this.shouldShowEmptySavedView);

      this.shouldShowEmptySavedView
        .filter(BooleanUtils::isTrue)
        .subscribe(this.animateHearts);

      this.currentUser.loggedInUser()
        .distinctUntilChanged((u1, u2) -> !UserUtils.userHasChanged(u1, u2))
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
        .compose(combineLatestPair(this.currentUser.isLoggedIn().distinctUntilChanged()))
        .compose(bindToLifecycle())
        .subscribe(paramsAndLoggedIn -> this.koala.trackDiscovery(
          paramsAndLoggedIn.first,
          isOnboardingVisible(paramsAndLoggedIn.first, paramsAndLoggedIn.second)
        ));

      this.startUpdateActivity
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

    private boolean isSavedVisible(final @NonNull DiscoveryParams params, final boolean isLoggedIn) {
      return isTrue(params.isSavedProjects()) && isLoggedIn;
    }

    private void saveLastSeenActivityId(final @Nullable Activity activity) {
      if (activity != null) {
        this.activitySamplePreference.set((int) activity.id());
      }
    }

    private final PublishSubject<Boolean> animateHearts = PublishSubject.create();
    private final PublishSubject<Boolean> activityClick = PublishSubject.create();
    private final PublishSubject<Project> activitySampleProjectClick = PublishSubject.create();
    private final PublishSubject<Activity> activityUpdateClick = PublishSubject.create();
    private final PublishSubject<Void> clearPage = PublishSubject.create();
    private final PublishSubject<Boolean> discoveryOnboardingLoginToutClick = PublishSubject.create();
    private final PublishSubject<Void> nextPage = PublishSubject.create();
    private final PublishSubject<DiscoveryParams> paramsFromActivity = PublishSubject.create();
    private final PublishSubject<Project> projectCardClicked = PublishSubject.create();
    private final PublishSubject<List<Category>> rootCategories = PublishSubject.create();

    private final BehaviorSubject<Activity> activity = BehaviorSubject.create();
    private final BehaviorSubject<List<Project>> projectList = BehaviorSubject.create();
    private final Observable<Boolean> showActivityFeed;
    private final Observable<Boolean> showLoginTout;
    private final BehaviorSubject<Boolean> shouldShowEmptySavedView = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> shouldShowOnboardingView = BehaviorSubject.create();
    private final Observable<Pair<Project, RefTag>> startProjectActivity;
    private final Observable<Activity> startUpdateActivity;

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
    @Override public void projectCardViewHolderClicked(final @NonNull Project project) {
      this.projectCardClicked.onNext(project);
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

    @Override public @NonNull Observable<Activity> activity() {
      return this.activity;
    }
    @Override public Observable<Boolean> animateHearts() {
      return this.animateHearts;
    }
    @Override public @NonNull Observable<List<Project>> projectList() {
      return this.projectList;
    }
    @Override public @NonNull Observable<Boolean> showActivityFeed() {
      return this.showActivityFeed;
    }
    @Override public @NonNull Observable<Boolean> showLoginTout() {
      return this.showLoginTout;
    }
    @Override public Observable<Boolean> shouldShowEmptySavedView() {
      return this.shouldShowEmptySavedView;
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
