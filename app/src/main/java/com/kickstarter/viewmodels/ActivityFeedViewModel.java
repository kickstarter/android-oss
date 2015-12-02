package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.viewmodels.inputs.ActivityFeedViewModelInputs;
import com.kickstarter.viewmodels.outputs.ActivityFeedViewModelOutputs;
import com.kickstarter.services.ActivityFeedParams;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.adapters.ActivityFeedAdapter;
import com.kickstarter.ui.viewholders.EmptyActivityFeedViewHolder;
import com.kickstarter.ui.viewholders.FriendBackingViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedPositiveViewHolder;
import com.kickstarter.ui.viewholders.ProjectStateChangedViewHolder;
import com.kickstarter.ui.viewholders.ProjectUpdateViewHolder;

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

    addSubscription(refresh
        .switchMap(__ -> activitiesWithPagination())
        .subscribe(activities)
    );

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

  private @NonNull Observable<List<Activity>> activitiesWithPagination() {

    return paramsWithPagination(ActivityFeedParams.builder().build())
      .concatMap(this::activitiesFromParams)
      .takeUntil(List::isEmpty)
      .scan(ListUtils::concat);
  }

  private @NonNull Observable<ActivityFeedParams> paramsWithPagination(final @NonNull ActivityFeedParams firstParams) {

    return moreActivitiesUrl
      .map(ActivityFeedParams::fromUrl)
      .compose(Transformers.takeWhen(nextPage))
      .startWith(firstParams);
  }

  private @NonNull Observable<List<Activity>> activitiesFromParams(@NonNull final ActivityFeedParams params) {
    return client.fetchActivities(params)
      .compose(Transformers.neverError())
      .doOnNext(this::keepPaginationParams)
      .map(ActivityEnvelope::activities)
      .doOnSubscribe(() -> isFetchingActivities.onNext(true))
      .finallyDo(() -> isFetchingActivities.onNext(false));
  }

  private void keepPaginationParams(@NonNull final ActivityEnvelope envelope) {
    final ActivityEnvelope.UrlsEnvelope urls = envelope.urls();
    if (urls != null) {
      final ActivityEnvelope.UrlsEnvelope.ApiEnvelope api = urls.api();
      if (api != null) {
        final String moreUrl = api.moreActivities();
        if (moreUrl != null) {
          moreActivitiesUrl.onNext(moreUrl);
        }
      }
    }
  }

  public void emptyActivityFeedDiscoverProjectsClicked(@NonNull final EmptyActivityFeedViewHolder viewHolder) {
    discoverProjectsClick.onNext(null);
  }

  public void emptyActivityFeedLoginClicked(@NonNull final EmptyActivityFeedViewHolder viewHolder) {
    loginClick.onNext(null);
  }

  public void friendBackingClicked(@NonNull final FriendBackingViewHolder viewHolder, @NonNull final Activity activity) {
    friendBackingClick.onNext(activity);
  }

  public void projectStateChangedClicked(@NonNull final ProjectStateChangedViewHolder viewHolder,
    @NonNull final Activity activity) {
    projectStateChangedClick.onNext(activity);
  }

  public void projectStateChangedPositiveClicked(@NonNull final ProjectStateChangedPositiveViewHolder viewHolder,
    @NonNull final Activity activity) {
    projectStateChangedPositiveClick.onNext(activity);
  }

  public void projectUpdateProjectClicked(@NonNull final ProjectUpdateViewHolder viewHolder,
    @NonNull final Activity activity) {
    projectUpdateProjectClick.onNext(activity);
  }

  public void projectUpdateClicked(@NonNull final ProjectUpdateViewHolder viewHolder,
    @NonNull final Activity activity) {
    projectUpdateUpdateClick.onNext(activity);
  }
}
