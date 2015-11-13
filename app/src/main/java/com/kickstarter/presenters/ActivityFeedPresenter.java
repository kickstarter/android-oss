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
  private final PublishSubject<Project> friendBackingClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Project> projectStateChangedPositiveClick = PublishSubject.create();
  private final PublishSubject<Project> projectStateChangedClick = PublishSubject.create();
  private final PublishSubject<Project> projectUpdateProjectClick = PublishSubject.create();
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
      .compose(Transformers.waitUntil(currentUser.loggedInUser()));

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
      .subscribe(vp -> vp.first.startProjectActivity(vp.second)));

    addSubscription(viewSubject
      .compose(Transformers.takeWhen(loginClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::activityFeedLogin));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectStateChangedClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second)));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectStateChangedPositiveClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second)));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectUpdateProjectClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second)));

    addSubscription(viewSubject
      .compose(Transformers.takePairWhen(projectUpdateUpdateClick))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showProjectUpdate(vp.second)));

    // kick off the first page of activities
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
      .map(ActivityEnvelope::activities);
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

  public void friendBackingClicked(@NonNull final FriendBackingViewHolder viewHolder, @NonNull final Project project) {
    friendBackingClick.onNext(project);
  }

  public void projectStateChangedClicked(@NonNull final ProjectStateChangedViewHolder viewHolder,
    @NonNull final Project project) {
    projectStateChangedClick.onNext(project);
  }

  public void projectStateChangedPositiveClicked(@NonNull final ProjectStateChangedPositiveViewHolder viewHolder,
    @NonNull final Project project) {
    projectStateChangedPositiveClick.onNext(project);
  }

  public void projectUpdateProjectClicked(@NonNull final ProjectUpdateViewHolder viewHolder,
    @NonNull final Project project) {
    projectUpdateProjectClick.onNext(project);
  }

  public void projectUpdateClicked(@NonNull final ProjectUpdateViewHolder viewHolder,
    @NonNull final Activity activity) {
    projectUpdateUpdateClick.onNext(activity);
  }
}
