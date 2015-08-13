package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.KsrApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.services.ActivityFeedParams;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.ActivityFeedActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class ActivityFeedPresenter extends Presenter<ActivityFeedActivity> {
  @Inject ApiClient client;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Activity>> activities = client.fetchActivities(new ActivityFeedParams())
      .map(envelope -> envelope.activities);

    final Observable<Pair<ActivityFeedActivity, List<Activity>>> viewAndActivities =
      RxUtils.combineLatestPair(viewSubject, activities);

    addSubscription(viewAndActivities
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(va -> va.first.onItemsNext(va.second)));
  }
}
