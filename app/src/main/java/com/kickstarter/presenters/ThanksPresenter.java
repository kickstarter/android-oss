package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.ThanksActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class ThanksPresenter extends Presenter<ThanksActivity> {
  private final PublishSubject<Void> doneClick = PublishSubject.create();
  private final PublishSubject<Void> facebookClick = PublishSubject.create();
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> twitterClick = PublishSubject.create();

  @Inject ApiClient apiClient;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public void takeProject(final Project project) {
    final Observable<Pair<ThanksActivity, Project>> viewAndProject = RxUtils.combineLatestPair(viewSubject, Observable.just(project))
      .filter(vp -> vp.first != null);

    addSubscription(viewAndProject
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.show(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, facebookClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startFacebookShareIntent(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, shareClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startShareIntent(vp.second)));

    addSubscription(RxUtils.takeWhen(viewAndProject, twitterClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.startTwitterShareIntent(vp.second)));

    addSubscription(RxUtils.combineLatestPair(viewSubject.filter(v -> v != null), doneClick)
      .map(vp -> vp.first)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ThanksActivity::startDiscoveryActivity));

    // TODO: Should use the project category root
    final DiscoveryParams params = new DiscoveryParams.Builder()
      .category(project.category())
      .backed(-1)
      .build();

    final Observable<List<Project>> recommendedProjects = apiClient.fetchProjects(params)
      .map(envelope -> envelope.projects);

    addSubscription(RxUtils.combineLatestPair(viewSubject.filter(v -> v != null), recommendedProjects)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showRecommendedProjects(vp.second)));
  }

  public void takeDoneClick() {
    doneClick.onNext(null);
  }

  public void takeFacebookClick() {
    facebookClick.onNext(null);
  }

  public void takeShareClick() {
    shareClick.onNext(null);
  }

  public void takeTwitterClick() {
    twitterClick.onNext(null);
  }

  // TODO: Hook this up
/*  public void onProjectClicked(final Project project, final MiniProjectsViewHolder viewHolder) {
    Timber.d("Project clicked");
  }*/
}
