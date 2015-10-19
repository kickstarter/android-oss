package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.utils.RxUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
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

public class ActivityFeedPresenter extends Presenter<ActivityFeedActivity> implements ActivityFeedAdapter.Delegate {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  private final PublishSubject<Project> discoverProjectsClick = PublishSubject.create();
  private final PublishSubject<Project> friendBackingClick = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Project> projectStateChangedPositiveClick = PublishSubject.create();
  private final PublishSubject<Project> projectStateChangedClick = PublishSubject.create();
  private final PublishSubject<Project> projectUpdateProjectClick = PublishSubject.create();
  private final PublishSubject<Activity> projectUpdateUpdateClick = PublishSubject.create();

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Activity>> activities = currentUser.observable()
      .filter(u -> u != null)
      .take(1)
      .flatMap(user -> client.fetchActivities(new ActivityFeedParams()))
      .map(ActivityEnvelope::activities);

    addSubscription(RxUtils.combineLatestPair(viewSubject, activities)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(va -> va.first.showActivities(va.second)));

    addSubscription(RxUtils.combineLatestPair(viewSubject, currentUser.observable())
      .filter(vu -> vu.second == null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vu -> vu.first.showEmptyFeed(vu.second)));

    addSubscription(RxUtils.takeWhen(viewSubject, discoverProjectsClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::discoverProjectsButtonOnClick));

    addSubscription(RxUtils.takePairWhen(viewSubject, friendBackingClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second)));

    addSubscription(RxUtils.takeWhen(viewSubject, loginClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ActivityFeedActivity::activityFeedLogin));

    addSubscription(RxUtils.takePairWhen(viewSubject, projectStateChangedClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second)));

    addSubscription(RxUtils.takePairWhen(viewSubject, projectStateChangedPositiveClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second)));

    addSubscription(RxUtils.takePairWhen(viewSubject, projectUpdateProjectClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startProjectActivity(vp.second)));

    addSubscription(RxUtils.takePairWhen(viewSubject, projectUpdateUpdateClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showProjectUpdate(vp.second)));
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
