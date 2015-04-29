package com.kickstarter.presenters;

import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.ProjectDetailActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

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
