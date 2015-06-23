package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;

import com.kickstarter.KsrApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.models.Activity;
import com.kickstarter.services.ActivityFeedParams;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.ActivityFeedActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class ActivityFeedPresenter extends Presenter<ActivityFeedActivity> {
  @Inject ApiClient client;
  private List<Activity> activities;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);

    activities = client.fetchActivities(new ActivityFeedParams())
      .map(envelope -> envelope.activities)
      .toBlocking().last(); // TODO: Don't block

    final Subscription subscription = viewSubject
      .filter(v -> v != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.onItemsNext(activities));
    addSubscription(subscription);
  }
}
