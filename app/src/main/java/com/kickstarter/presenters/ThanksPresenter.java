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
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.ThanksActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class ThanksPresenter extends Presenter<ThanksActivity> {
  private final PublishSubject<Void> shareClick = PublishSubject.create();
  private final PublishSubject<Void> doneClick = PublishSubject.create();

  @Inject ApiClient apiClient;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);
  }

  public void takeProject(final Project project) {
    final Observable<Pair<ThanksActivity, Project>> viewAndProject = RxUtils.combineLatestPair(viewSubject, Observable.just(project))
      .filter(vp -> vp.first != null);

    addSubscription(viewAndProject
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.show(vp.second)));

    addSubscription(shareClick.withLatestFrom(viewAndProject, (click, pair) -> pair)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> share(vp.first, vp.second)));

    addSubscription(doneClick.withLatestFrom(viewAndProject, (click, pair) -> pair)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> done(vp.first)));

    // TODO: Should use the project category root
    DiscoveryParams params = new DiscoveryParams.Builder()
      .category(project.category())
      .backed(-1)
      .build();
    Observable<List<Project>> recommendedProjects = apiClient.fetchProjects(params)
      .map(envelope -> envelope.projects);

    addSubscription(RxUtils.combineLatestPair(viewSubject.filter(v -> v != null), recommendedProjects)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.showRecommendedProjects(vp.second)));
  }

  public void takeDoneClick() {
    doneClick.onNext(null);
  }

  public void takeShareClick() {
    Timber.d("takeShareClick");
    shareClick.onNext(null);
  }

  private void share(final Context context, final Project project) {
    Timber.d("Share intent");
    final Intent intent = new Intent(android.content.Intent.ACTION_SEND)
      .setType("text/plain")
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
      .putExtra(Intent.EXTRA_TEXT, context.getResources()
        .getString(R.string.I_just_backed_project_on_Kickstarter, project.name(), project
          .secureWebProjectUrl()));

    context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.Share_this_project)));
  }

  private void done(final Context context) {
    Timber.d("Done intent");
    final Intent intent = new Intent(context, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    context.startActivity(intent);
  }

  // TODO: Hook this up
/*  public void onProjectClicked(final Project project, final MiniProjectsViewHolder viewHolder) {
    Timber.d("Project clicked");
  }*/
}
