package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ActivityFeedViewModel extends ViewModel<ActivityFeedActivity> implements ActivityFeedAdapter.Delegate,
  ActivityFeedViewModelInputs, ActivityFeedViewModelOutputs {
  private final ApiClientType client;
  private final CurrentUserType currentUser;

  private final PublishSubject<Project> discoverProjectsClick = PublishSubject.create();
  private final PublishSubject<Activity> friendBackingClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Activity> projectStateChangedPositiveClick = PublishSubject.create();
  private final PublishSubject<Activity> projectStateChangedClick = PublishSubject.create();
  private final PublishSubject<Activity> projectUpdateProjectClick = PublishSubject.create();
  private final PublishSubject<Activity> projectUpdateUpdateClick = PublishSubject.create();

  private final PublishSubject<String> moreActivitiesUrl = PublishSubject.create();

  // INPUTS
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }
  private final PublishSubject<Void> refresh = PublishSubject.create();
  public void refresh() {
    refresh.onNext(null);
  }
  public final ActivityFeedViewModelInputs inputs = this;

  // OUTPUTS
  private final BehaviorSubject<Boolean> loggedOutEmptyStateIsVisible = BehaviorSubject.create();
  public Observable<Boolean> loggedOutEmptyStateIsVisible() {
    return loggedOutEmptyStateIsVisible;
  }
  private final BehaviorSubject<Boolean> loggedInEmptyStateIsVisible = BehaviorSubject.create();
  public Observable<Boolean> loggedInEmptyStateIsVisible() {
    return loggedInEmptyStateIsVisible;
  }
  private final PublishSubject<Boolean> isFetchingActivities = PublishSubject.create();
  public Observable<Boolean> isFetchingActivities() {
    return isFetchingActivities;
  }
  private final BehaviorSubject<List<Activity>> activities = BehaviorSubject.create();
  public Observable<List<Activity>> activities() {
    return activities;
  }
  public final ActivityFeedViewModelOutputs outputs = this;

  public ActivityFeedViewModel(final @NonNull Environment environment) {
    super(environment);

    this.client = environment.apiClient();
    this.currentUser = environment.currentUser();
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

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
      .subscribe(loggedOutEmptyStateIsVisible::onNext);

    currentUser.observable()
      .compose(Transformers.takePairWhen(activities))
      .map(ua -> ua.first != null && ua.second.size() == 0)
      .compose(bindToLifecycle())
      .subscribe(loggedInEmptyStateIsVisible::onNext);

    view()
      .compose(Transformers.takeWhen(discoverProjectsClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(ActivityFeedActivity::discoverProjectsButtonOnClick);

    view()
      .compose(Transformers.takePairWhen(friendBackingClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project()));

    view()
      .compose(Transformers.takeWhen(loginClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(ActivityFeedActivity::activityFeedLogin);

    view()
      .compose(Transformers.takePairWhen(projectStateChangedClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project()));

    view()
      .compose(Transformers.takePairWhen(projectStateChangedPositiveClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project()));

    view()
      .compose(Transformers.takePairWhen(projectUpdateProjectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project()));

    view()
      .compose(Transformers.takePairWhen(projectUpdateUpdateClick))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vp -> vp.first.showProjectUpdate(vp.second));

    // Track viewing and paginating activity.
    nextPage
      .compose(Transformers.incrementalCount())
      .compose(bindToLifecycle())
      .subscribe(koala::trackActivityView);

    // Track tapping on any of the activity items.
    Observable.merge(
      friendBackingClick,
      projectStateChangedPositiveClick,
      projectStateChangedClick,
      projectUpdateProjectClick,
      projectUpdateUpdateClick)
      .compose(bindToLifecycle())
      .subscribe(koala::trackActivityTapped);
  }

  public void emptyActivityFeedDiscoverProjectsClicked(final @NonNull EmptyActivityFeedViewHolder viewHolder) {
    discoverProjectsClick.onNext(null);
  }

  public void emptyActivityFeedLoginClicked(final @NonNull EmptyActivityFeedViewHolder viewHolder) {
    loginClick.onNext(null);
  }

  public void friendBackingClicked(final @NonNull FriendBackingViewHolder viewHolder, final @NonNull Activity activity) {
    friendBackingClick.onNext(activity);
  }

  public void projectStateChangedClicked(final @NonNull ProjectStateChangedViewHolder viewHolder,
    final @NonNull Activity activity) {
    projectStateChangedClick.onNext(activity);
  }

  public void projectStateChangedPositiveClicked(final @NonNull ProjectStateChangedPositiveViewHolder viewHolder,
    final @NonNull Activity activity) {
    projectStateChangedPositiveClick.onNext(activity);
  }

  public void projectUpdateProjectClicked(final @NonNull ProjectUpdateViewHolder viewHolder,
    final @NonNull Activity activity) {
    projectUpdateProjectClick.onNext(activity);
  }

  public void projectUpdateClicked(final @NonNull ProjectUpdateViewHolder viewHolder,
    final @NonNull Activity activity) {
    projectUpdateUpdateClick.onNext(activity);
  }
}
