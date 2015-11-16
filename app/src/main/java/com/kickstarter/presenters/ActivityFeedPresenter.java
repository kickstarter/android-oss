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
import com.kickstarter.models.Project;
import com.kickstarter.presenters.inputs.ActivityFeedPresenterInputs;
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
import rx.subjects.PublishSubject;

public final class ActivityFeedPresenter extends Presenter<ActivityFeedActivity> implements ActivityFeedAdapter.Delegate, ActivityFeedPresenterInputs {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  private final PublishSubject<Project> discoverProjectsClick = PublishSubject.create();
  private final PublishSubject<Activity> friendBackingClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Activity> projectStateChangedPositiveClick = PublishSubject.create();
  private final PublishSubject<Activity> projectStateChangedClick = PublishSubject.create();
  private final PublishSubject<Activity> projectUpdateProjectClick = PublishSubject.create();
  private final PublishSubject<Activity> projectUpdateUpdateClick = PublishSubject.create();

  private final PublishSubject<ActivityFeedParams> params = PublishSubject.create();

  // Inputs
  private final PublishSubject<Void> nextPage = PublishSubject.create();
  public void nextPage() {
    nextPage.onNext(null);
  }
  public final ActivityFeedPresenterInputs inputs = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Activity>> activities = activitiesWithPagination()
      .compose(Transformers.waitUntil(currentUser.loggedInUser()))
      .share();

    addSubscription(viewSubject
      .compose(Transformers.combineLatestPair(activities))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(va -> va.first.showActivities(va.second)));

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
      .compose(Transformers.takePairWhen(friendBackingClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(viewSubject
      .compose(Transformers.takeWhen(loginClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::activityFeedLogin));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectStateChangedClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectStateChangedPositiveClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectUpdateProjectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second.project())));

    addSubscription(viewSubject
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
      .doOnNext(this::keepMoreActivitiesUrl)
      .map(ActivityEnvelope::activities);
  }

  private void keepMoreActivitiesUrl(@NonNull final ActivityEnvelope envelope) {
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
