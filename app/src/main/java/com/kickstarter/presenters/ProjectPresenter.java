package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.ui.adapters.ProjectAdapter;
import com.kickstarter.ui.viewholders.RewardViewHolder;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class ProjectPresenter extends Presenter<ProjectActivity> implements ProjectAdapter.Delegate {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;
  private final PublishSubject<Void> backProjectClick = PublishSubject.create();
  private final PublishSubject<Void> blurbClick = PublishSubject.create();
  private final PublishSubject<Void> commentsClick = PublishSubject.create();
  private final PublishSubject<Void> creatorNameClick = PublishSubject.create();
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> updatesClick = PublishSubject.create();
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<Reward> rewardClick = PublishSubject.create();
  private final PublishSubject<Void> starClick = PublishSubject.create();

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public void initialize(@Nullable final Project initialProject, @Nullable final String param) {
    final Observable<Reward> rewardOnLoggedInUserClick = RxUtils.takePairWhen(currentUser.observable(), rewardClick)
      .filter(ur -> ur.first != null)
      .map(ur -> ur.second);

    final Observable<User> loggedOutUserOnRewardClick = RxUtils.takeWhen(currentUser.observable(), rewardClick)
      .filter(u -> u == null);

    final Observable<User> loggedInUserOnStarClick = RxUtils.takeWhen(currentUser.observable(), starClick)
      .filter(u -> u != null);

    final Observable<User> loggedOutUserOnStarClick = RxUtils.takeWhen(currentUser.observable(), starClick)
      .filter(u -> u == null);

    final Observable<Project> projectOnUserChangeStar = loggedInUserOnStarClick
      .switchMap(__ -> toggleProjectStar(initialProject))
      .share();

    final Observable<Project> starredProjectOnLoginSuccess = loginSuccess
      .take(1)
      .switchMap(__ -> starProject(initialProject))
      .share();

    final Observable<Project> project = (initialProject != null ? client.fetchProject(initialProject) : client.fetchProject(param))
      .mergeWith(projectOnUserChangeStar)
      .mergeWith(starredProjectOnLoginSuccess)
      .share();

    final Observable<Pair<ProjectActivity, Project>> viewAndProject =
      RxUtils.combineLatestPair(viewSubject, project);

    addSubscription(
      viewAndProject
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.show(vp.second))
    );

    addSubscription(
      RxUtils.takePairWhen(
        viewSubject,
        projectOnUserChangeStar.mergeWith(starredProjectOnLoginSuccess)
      )
        .filter(vp -> vp.second.isStarred())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vp -> vp.first.showStarPrompt())
    );

    addSubscription(
      RxUtils.takeWhen(viewSubject, loggedOutUserOnStarClick)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(ProjectActivity::startLoginToutActivity)
    );

    addSubscription(
      RxUtils.takeWhen(viewSubject, loggedOutUserOnRewardClick)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(ProjectActivity::startLoginToutActivity)
    );

    addSubscription(
      RxUtils.takePairWhen(viewAndProject, rewardOnLoggedInUserClick)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vpr -> {
          final ProjectActivity view = vpr.first.first;
          final Project p = vpr.first.second;
          final Reward r = vpr.second;
          view.startRewardSelectedCheckout(p, r);
        })
    );

    // todo loginSuccess with reward click

    addSubscription(RxUtils.takeWhen(viewAndProject, backProjectClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startCheckoutActivity(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, shareClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startShareIntent(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, blurbClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showProjectDescription(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, commentsClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startCommentsActivity(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, creatorNameClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showCreatorBio(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, updatesClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showUpdates(vp.second)));
  }

  public void takeBackProjectClick() {
    backProjectClick.onNext(null);
  }

  public void takeBlurbClick() {
    blurbClick.onNext(null);
  }

  public void takeCommentsClick() {
    commentsClick.onNext(null);
  }

  public void takeCreatorNameClick(){
    creatorNameClick.onNext(null);
  }

  public void takeRewardClick(@NonNull final RewardViewHolder viewHolder, @NonNull final Reward reward) {
    rewardClick.onNext(reward);
  }

  public void takeShareClick() {
    shareClick.onNext(null);
  }

  public void takeStarClick() {
    starClick.onNext(null);
  }

  public void takeUpdatesClick() {
    updatesClick.onNext(null);
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }

  public Observable<Project> starProject(@NonNull final Project project) {
    return client.starProject(project)
      .onErrorResumeNext(Observable.empty());
  }

  public Observable<Project> toggleProjectStar(@NonNull final Project project) {
    return client.toggleProjectStar(project)
      .onErrorResumeNext(Observable.empty());
  }
}
