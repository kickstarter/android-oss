package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.ProjectActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;

public class ProjectPresenter extends Presenter<ProjectActivity> {
  @Inject ApiClient client;
  private final PublishSubject<Void> backProjectClick = PublishSubject.create();
  private final PublishSubject<Void> blurbClick = PublishSubject.create();
  private final PublishSubject<Void> commentsClick = PublishSubject.create();
  private final PublishSubject<Void> creatorNameClick = PublishSubject.create();
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> updatesClick = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public void takeProject(final Project project) {
    final ConnectableObservable<Project> latestProject = client.fetchProject(project)
      .filter(Project::isDisplayable)
      .publish();

    final Observable<Pair<ProjectActivity, Project>> viewAndProject =
      RxUtils.combineLatestPair(viewSubject, latestProject);

    addSubscription(viewAndProject
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.show(vp.second)));

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

    addSubscription(latestProject.connect());
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
}
