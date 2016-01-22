package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.CurrentUser;
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

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ActivityFeedViewModel extends ViewModel<ActivityFeedActivity> implements ActivityFeedAdapter.Delegate,
  ActivityFeedViewModelInputs, ActivityFeedViewModelOutputs {
  protected @Inject ApiClientType client;
  protected @Inject CurrentUser currentUser;

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
  private final BehaviorSubject<Boolean> showLoggedOutEmptyState = BehaviorSubject.create();
  public final Observable<Boolean> showLoggedOutEmptyState() {
    return showLoggedOutEmptyState;
  }
  private final PublishSubject<Boolean> isFetchingActivities = PublishSubject.create();
  public final Observable<Boolean> isFetchingActivities() {
    return isFetchingActivities;
  }
  private final BehaviorSubject<List<Activity>> activities = BehaviorSubject.create();
  public final Observable<List<Activity>> activities() {
    return activities;
  }
  public final ActivityFeedViewModelOutputs outputs = this;

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final ApiPaginator<Activity, ActivityEnvelope, Void> paginator = ApiPaginator.<Activity, ActivityEnvelope, Void>builder()
      .nextPage(nextPage)
      .startOverWith(refresh)
      .envelopeToListOfData(ActivityEnvelope::activities)
      .envelopeToMoreUrl(env -> env.urls().api().moreActivities())
      .loadWithParams(__ -> client.fetchActivities())
      .loadWithPaginationPath(client::fetchActivitiesWithPaginationPath)
      .build();

    addSubscription(paginator.paginatedData.subscribe(activities));
    addSubscription(paginator.isFetching.subscribe(isFetchingActivities));

    addSubscription(currentUser.loggedInUser()
        .take(1)
        .subscribe(__ -> refresh())
    );

    addSubscription(currentUser.isLoggedIn().subscribe(showLoggedOutEmptyState));

    addSubscription(view
      .compose(Transformers.takeWhen(discoverProjectsClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::discoverProjectsButtonOnClick));

    addSubscription(view
      .compose(Transformers.takePairWhen(friendBackingClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(view
      .compose(Transformers.takeWhen(loginClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::activityFeedLogin));

    addSubscription(view
      .compose(Transformers.takePairWhen(projectStateChangedClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(view
      .compose(Transformers.takePairWhen(projectStateChangedPositiveClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(view
      .compose(Transformers.takePairWhen(projectUpdateProjectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(view
      .compose(Transformers.takePairWhen(projectUpdateUpdateClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showProjectUpdate(vp.second)));

    // Track viewing and paginating activity.
    addSubscription(nextPage
        .compose(Transformers.incrementalCount())
        .subscribe(koala::trackActivityView)
    );

    // Track tapping on any of the activity items.
    addSubscription(
      Observable.merge(
        friendBackingClick,
        projectStateChangedPositiveClick,
        projectStateChangedClick,
        projectUpdateProjectClick,
        projectUpdateUpdateClick
      ).subscribe(koala::trackActivityTapped)
    );
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
