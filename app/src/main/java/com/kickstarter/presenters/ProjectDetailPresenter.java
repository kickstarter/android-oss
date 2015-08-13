package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.activities.ProjectDetailActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class ProjectDetailPresenter extends Presenter<ProjectDetailActivity> {
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
    ((KsrApplication) context.getApplicationContext()).component().inject(this);
  }

  // todo: cut the repetition
  public void takeProject(final Project project) {
    final Observable<Project> latestProject = Observable.merge(Observable.just(project), client.fetchProject(project));
    final Observable<Pair<ProjectDetailActivity, Project>> viewAndProject =
      RxUtils.combineLatestPair(viewSubject, latestProject);

    addSubscription(RxUtils.combineLatestPair(latestProject, viewSubject)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(pair -> pair.second.show(pair.first)));

    addSubscription(RxUtils.takeWhen(viewAndProject, backProjectClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startCheckoutActivity(vp.second)));

    addSubscription(RxUtils.combineLatestPair(latestProject, shareClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(pair -> share(pair.first)));

    addSubscription(blurbClick.withLatestFrom(viewAndProject, (click, pair) -> pair)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showProjectDescription(vp.second)));

    addSubscription(commentsClick.withLatestFrom(viewAndProject, (click, pair) -> pair)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showComments(vp.second)));

    addSubscription(creatorNameClick.withLatestFrom(viewAndProject, (click, pair) -> pair)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showCreatorBio(vp.second)));

    addSubscription(updatesClick.withLatestFrom(viewAndProject, (click, pair) -> pair)
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

  protected void back(final Project project) {
    final Intent intent = new Intent(view(), CheckoutActivity.class);
    intent.putExtra("project", project);
    intent.putExtra("url", project.newPledgeUrl());
    view().startActivity(intent);
    view().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  // todo: limit the apps you can share to, format string
  private void share(final Project project) {
    final Intent intent = new Intent(Intent.ACTION_SEND)
      .setType("text/plain")
      .putExtra(Intent.EXTRA_TEXT, project.name() + ", via " + project.urls().web().project());
    view().startActivity(intent);
  }
}
