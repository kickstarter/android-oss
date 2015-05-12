package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;

import com.kickstarter.KsrApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterClient;
import com.kickstarter.ui.activities.ProjectDetailActivity;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class ProjectDetailPresenter extends Presenter<ProjectDetailActivity> {
  @Inject KickstarterClient client;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);
  }

  public void takeProject(final Project project) {
    Subscription subscription = RxUtils.combineLatestPair(client.fetchProject(project), viewSubject)
      .filter(v -> v.second != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.second.show(v.first));
    addSubscription(subscription);
  }
}
