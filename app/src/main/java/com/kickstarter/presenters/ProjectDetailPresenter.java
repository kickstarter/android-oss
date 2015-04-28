package com.kickstarter.presenters;

import android.os.Bundle;

import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.ProjectDetailActivity;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class ProjectDetailPresenter extends Presenter<ProjectDetailActivity> {
  private static final KickstarterClient client = new KickstarterClient();

  public void takeProject(final Project project) {
    Subscription subscription = RxUtils.combineLatestPair(client.fetchProject(project), viewSubject)
      .filter(v -> v.second != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.second.show(v.first));
    subscriptions.add(subscription);
  }
}
