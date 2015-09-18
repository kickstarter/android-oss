package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.ProjectActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class ProjectPresenter extends Presenter<ProjectActivity> {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;
  private final PublishSubject<Void> backProjectClick = PublishSubject.create();
  private final PublishSubject<Void> blurbClick = PublishSubject.create();
  private final PublishSubject<Void> commentsClick = PublishSubject.create();
  private final PublishSubject<Void> creatorNameClick = PublishSubject.create();
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> updatesClick = PublishSubject.create();
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<Void> starClick = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public void takeProject(final Project initialProject) {

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

    final Observable<Project> project = client.fetchProject(initialProject)
      .mergeWith(projectOnUserChangeStar)
      .mergeWith(starredProjectOnLoginSuccess)
      .filter(Project::isDisplayable)
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

  public void takeShareClick() {
    shareClick.onNext(null);
  }

  public void takeUpdatesClick() {
    updatesClick.onNext(null);
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }

  public void takeStarClick() {
    starClick.onNext(null);
  }

  public Observable<Project> starProject(final Project project) {
    return client.starProject(project)
      .onErrorResumeNext(Observable.empty());
  }

  public Observable<Project> toggleProjectStar(final Project project) {
    return client.toggleProjectStar(project)
      .onErrorResumeNext(Observable.empty());
  }
}
