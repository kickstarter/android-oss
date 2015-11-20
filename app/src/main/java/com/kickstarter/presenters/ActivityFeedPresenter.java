package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.inputs.ActivityFeedPresenterInputs;
import com.kickstarter.presenters.outputs.ActivityFeedPresenterOutputs;
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

public final class ActivityFeedPresenter extends Presenter<ActivityFeedActivity> implements ActivityFeedAdapter.Delegate,
  ActivityFeedPresenterInputs, ActivityFeedPresenterOutputs {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  private final PublishSubject<Project> discoverProjectsClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Activity> projectClick = PublishSubject.create();
  private final PublishSubject<Activity> updateClick = PublishSubject.create();

  private final PublishSubject<ActivityFeedParams> params = PublishSubject.create();

  // INPUTS
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }
  private final BehaviorSubject<Empty> refresh = BehaviorSubject.create(Empty.create());
  public void refresh() {
    refresh.onNext(Empty.create());
  }
  public final ActivityFeedPresenterInputs inputs = this;

  // OUTPUTS
  private final PublishSubject<Boolean> isFetchingActivities = PublishSubject.create();
  public final Observable<Boolean> isFetchingActivities() {
    return isFetchingActivities;
  }
  private final BehaviorSubject<List<Activity>> activities = BehaviorSubject.create();
  public final Observable<List<Activity>> activities() {
    return activities;
  }
  public final ActivityFeedPresenterOutputs outputs = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    addSubscription(refresh
      .switchMap(__ -> activitiesWithPagination())
      .compose(Transformers.waitUntil(currentUser.loggedInUser()))
      .subscribe(activities::onNext));

    addSubscription(refresh
      .subscribe(__ -> {
        params.onNext(ActivityFeedParams.builder().build());
        nextPage.onNext(null);
      }));

    addSubscription(viewSubject
      .compose(Transformers.combineLatestPair(currentUser.observable()))
      .filter(vu -> vu.second == null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vu -> vu.first.showEmptyFeed(vu.second)));

    addSubscription(viewSubject
      .compose(Transformers.combineLatestPair(currentUser.observable()))
      .filter(vu -> vu.second == null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vu -> vu.first.showEmptyFeed(vu.second)));

    addSubscription(viewSubject
      .compose(Transformers.takeWhen(discoverProjectsClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::discoverProjectsButtonOnClick));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(viewSubject
      .compose(Transformers.takeWhen(loginClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::activityFeedLogin));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(viewSubject
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

    // kick off the first page of activities. should be last.
    params.onNext(ActivityFeedParams.builder().build());
    nextPage.onNext(null);
  }

  private Observable<List<Activity>> activitiesWithPagination() {
    return params
      .compose(Transformers.takeWhen(nextPage))
      .concatMap(this::activitiesFromParams)
      .takeUntil(List::isEmpty)
      .scan(ListUtils::concat);
  }

  @NonNull private Observable<List<Activity>> activitiesFromParams(@NonNull final ActivityFeedParams params) {
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
          this.params.onNext(ActivityFeedParams.fromUrl(moreUrl));
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
