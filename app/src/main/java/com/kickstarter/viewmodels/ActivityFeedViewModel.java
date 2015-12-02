package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.ApiPaginator;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClient;
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
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  private final PublishSubject<Project> discoverProjectsClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Activity> projectClick = PublishSubject.create();
  private final PublishSubject<Activity> updateClick = PublishSubject.create();

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
  private final BehaviorSubject<User> loggedOutEmptyState = BehaviorSubject.create();
  public final Observable<User> loggedOutEmptyState() {
    return loggedOutEmptyState;
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
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final ApiPaginator<Activity, ActivityEnvelope, Void> paginator = ApiPaginator.<Activity, ActivityEnvelope, Void>builder()
      .nextPage(nextPage)
      .startOverWith(refresh)
      .envelopeToListOfData(ActivityEnvelope::activities)
      .envelopeToMoreUrl(env -> env.urls().api().moreActivities())
      .loadWithParams(__ -> client.fetchActivities())
      .loadWithPaginationPath(client::fetchActivities)
      .build();

    paginator.paginatedData.subscribe(activities);
    paginator.isFetching.subscribe(isFetchingActivities);

    addSubscription(currentUser.loggedInUser()
        .take(1)
        .subscribe(__ -> refresh())
    );

    addSubscription(currentUser.loggedOutUser()
        .subscribe(__ -> loggedOutEmptyState.onNext(null))
    );

    addSubscription(view
      .compose(Transformers.takeWhen(discoverProjectsClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::discoverProjectsButtonOnClick));

    addSubscription(view
      .compose(Transformers.takePairWhen(projectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(view
      .compose(Transformers.takeWhen(loginClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::activityFeedLogin));

    addSubscription(view
      .compose(Transformers.takePairWhen(projectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(view
      .compose(Transformers.takePairWhen(projectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(view
      .compose(Transformers.takePairWhen(projectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(view
      .compose(Transformers.takePairWhen(updateClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showProjectUpdate(vp.second)));

    // Track viewing and paginating activity.
    addSubscription(nextPage
        .compose(Transformers.incrementalCount())
        .subscribe(koala::trackActivityView)
    );

    // Track tapping on any of the activity items.
    addSubscription(projectClick.mergeWith(updateClick)
        .subscribe(koala::trackActivityTapped)
    );
  }

  public void emptyActivityFeedDiscoverProjectsClicked(@NonNull final EmptyActivityFeedViewHolder viewHolder) {
    discoverProjectsClick.onNext(null);
  }

  public void emptyActivityFeedLoginClicked(@NonNull final EmptyActivityFeedViewHolder viewHolder) {
    loginClick.onNext(null);
  }

  public void friendBackingClicked(@NonNull final FriendBackingViewHolder viewHolder, @NonNull final Activity activity) {
    projectClick.onNext(activity);
  }

  public void projectStateChangedClicked(@NonNull final ProjectStateChangedViewHolder viewHolder,
    @NonNull final Activity activity) {
    projectClick.onNext(activity);
  }

  public void projectStateChangedPositiveClicked(@NonNull final ProjectStateChangedPositiveViewHolder viewHolder,
    @NonNull final Activity activity) {
    projectClick.onNext(activity);
  }

  public void projectUpdateProjectClicked(@NonNull final ProjectUpdateViewHolder viewHolder,
    @NonNull final Activity activity) {
    projectClick.onNext(activity);
  }

  public void projectUpdateClicked(@NonNull final ProjectUpdateViewHolder viewHolder,
    @NonNull final Activity activity) {
    updateClick.onNext(activity);
  }
}
