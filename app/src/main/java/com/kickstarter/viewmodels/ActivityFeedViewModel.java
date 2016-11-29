package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.adapters.ActivityFeedAdapter;
import com.kickstarter.ui.viewholders.EmptyActivityFeedViewHolder;
import com.kickstarter.ui.viewholders.FriendBackingViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder;
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder;
import com.kickstarter.viewmodels.inputs.ActivityFeedViewModelInputs;
import com.kickstarter.viewmodels.outputs.ActivityFeedViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ActivityFeedViewModel extends ActivityViewModel<ActivityFeedActivity> implements ActivityFeedAdapter.Delegate,
  ActivityFeedViewModelInputs, ActivityFeedViewModelOutputs {
  private final ApiClientType client;
  private final CurrentUserType currentUser;

  public ActivityFeedViewModel(final @NonNull Environment environment) {
    super(environment);

    client = environment.apiClient();
    currentUser = environment.currentUser();

    goToDiscovery = discoverProjectsClick;
    goToLogin = loginClick;
    goToProjectUpdate = projectUpdateClick;
    goToProject = Observable.merge(
      friendBackingClick,
      projectStateChangedClick,
      projectStateChangedPositiveClick,
      projectUpdateProjectClick
    )
      .map(Activity::project);

    final ApiPaginator<Activity, ActivityEnvelope, Void> paginator = ApiPaginator.<Activity, ActivityEnvelope, Void>builder()
      .nextPage(nextPage)
      .startOverWith(refresh)
      .envelopeToListOfData(ActivityEnvelope::activities)
      .envelopeToMoreUrl(env -> env.urls().api().moreActivities())
      .loadWithParams(__ -> client.fetchActivities())
      .loadWithPaginationPath(client::fetchActivitiesWithPaginationPath)
      .build();

    paginator.paginatedData()
      .compose(bindToLifecycle())
      .subscribe(activities);

    paginator.isFetching()
      .compose(bindToLifecycle())
      .subscribe(isFetchingActivities);

    currentUser.loggedInUser()
      .take(1)
      .compose(bindToLifecycle())
      .subscribe(__ -> refresh());

    currentUser.isLoggedIn()
      .map(loggedIn -> !loggedIn)
      .compose(bindToLifecycle())
      .subscribe(loggedOutEmptyStateIsVisible);

    currentUser.observable()
      .compose(Transformers.takePairWhen(activities))
      .map(ua -> ua.first != null && ua.second.size() == 0)
      .compose(bindToLifecycle())
      .subscribe(loggedInEmptyStateIsVisible);

    // Track viewing and paginating activity.
    nextPage
      .compose(Transformers.incrementalCount())
      .startWith(0)
      .compose(bindToLifecycle())
      .subscribe(koala::trackActivityView);

    // Track tapping on any of the activity items.
    Observable.merge(
      friendBackingClick,
      projectStateChangedPositiveClick,
      projectStateChangedClick,
      projectUpdateProjectClick
    )
      .compose(bindToLifecycle())
      .subscribe(koala::trackActivityTapped);
  }

  private final PublishSubject<Void> discoverProjectsClick = PublishSubject.create();
  private final PublishSubject<Activity> friendBackingClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  private final PublishSubject<Activity> projectStateChangedClick = PublishSubject.create();
  private final PublishSubject<Activity> projectStateChangedPositiveClick = PublishSubject.create();
  private final PublishSubject<Activity> projectUpdateClick = PublishSubject.create();
  private final PublishSubject<Activity> projectUpdateProjectClick = PublishSubject.create();
  private final PublishSubject<Void> refresh = PublishSubject.create();

  private final BehaviorSubject<List<Activity>> activities = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> isFetchingActivities= BehaviorSubject.create();
  private final BehaviorSubject<Boolean> loggedInEmptyStateIsVisible = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> loggedOutEmptyStateIsVisible = BehaviorSubject.create();
  private final Observable<Void> goToDiscovery;
  private final Observable<Void> goToLogin;
  private final Observable<Project> goToProject;
  private final Observable<Activity> goToProjectUpdate;

  public final ActivityFeedViewModelInputs inputs = this;
  public final ActivityFeedViewModelOutputs outputs = this;

  @Override public void emptyActivityFeedDiscoverProjectsClicked(final @NonNull EmptyActivityFeedViewHolder viewHolder) {
    discoverProjectsClick.onNext(null);
  }

  @Override public void emptyActivityFeedLoginClicked(final @NonNull EmptyActivityFeedViewHolder viewHolder) {
    loginClick.onNext(null);
  }

  @Override public void friendBackingClicked(final @NonNull FriendBackingViewHolder viewHolder, final @NonNull Activity activity) {
    friendBackingClick.onNext(activity);
  }

  @Override public void nextPage() {
    nextPage.onNext(null);
  }

  @Override public void projectStateChangedClicked(final @NonNull ProjectStateChangedViewHolder viewHolder,
    final @NonNull Activity activity) {
    projectStateChangedClick.onNext(activity);
  }

  @Override public void projectStateChangedPositiveClicked(final @NonNull ProjectStateChangedPositiveViewHolder viewHolder,
    final @NonNull Activity activity) {
    projectStateChangedPositiveClick.onNext(activity);
  }

  @Override public void projectUpdateClicked(final @NonNull ProjectUpdateViewHolder viewHolder,
    final @NonNull Activity activity) {
    projectUpdateClick.onNext(activity);
  }

  @Override public void projectUpdateProjectClicked(final @NonNull ProjectUpdateViewHolder viewHolder,
    final @NonNull Activity activity) {
    projectUpdateProjectClick.onNext(activity);
  }

  @Override public void refresh() {
    refresh.onNext(null);
  }

  @Override public @NonNull Observable<List<Activity>> activities() {
    return activities;
  }

  @Override public @NonNull Observable<Boolean> isFetchingActivities() {
    return isFetchingActivities;
  }

  @Override public @NonNull Observable<Boolean> loggedInEmptyStateIsVisible() {
    return loggedInEmptyStateIsVisible;
  }

  @Override public @NonNull Observable<Boolean> loggedOutEmptyStateIsVisible() {
    return loggedOutEmptyStateIsVisible;
  }

  @Override public @NonNull Observable<Void> goToDiscovery() {
    return goToDiscovery;
  }

  @Override public @NonNull Observable<Void> goToLogin() {
    return goToLogin;
  }

  @Override public @NonNull Observable<Project> goToProject() {
    return goToProject;
  }

  @Override public @NonNull Observable<Activity> goToProjectUpdate() {
    return goToProjectUpdate;
  }
}
