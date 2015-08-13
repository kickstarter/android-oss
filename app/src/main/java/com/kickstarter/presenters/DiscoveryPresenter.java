package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KsrApplication;
import com.kickstarter.libs.BuildCheck;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.DiscoveryActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class DiscoveryPresenter extends Presenter<DiscoveryActivity> {
  @Inject ApiClient apiClient;
  @Inject KickstarterClient kickstarterClient;
  @Inject BuildCheck buildCheck;

  private final PublishSubject<Project> projectClick = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);

    buildCheck.bind(this, kickstarterClient);

    DiscoveryParams initialParams = DiscoveryParams.params();

    final Observable<List<Project>> projects = apiClient.fetchProjects(initialParams)
      .map(envelope -> envelope.projects);

    final Observable<Pair<DiscoveryActivity, List<Project>>> viewAndProjects =
      RxUtils.combineLatestPair(viewSubject, projects);

    addSubscription(viewAndProjects
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vp -> vp.first.onItemsNext(vp.second)));

    addSubscription(RxUtils.takePairWhen(viewSubject, projectClick)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(viewAndProject -> viewAndProject.first.startProjectDetailActivity(viewAndProject.second))
    );
  }

  public void takeProjectClick(final Project project) {
    projectClick.onNext(project);
  }
}
