package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.utils.RxUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.services.ActivityFeedParams;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.ActivityEnvelope;
import com.kickstarter.ui.activities.ActivityFeedActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class ActivityFeedPresenter extends Presenter<ActivityFeedActivity> {
  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Activity>> activities = currentUser.loggedInUser()
      .take(1)
      .flatMap(user -> client.fetchActivities(new ActivityFeedParams()))
      .map(ActivityEnvelope::activities);

    final Observable<Pair<ActivityFeedActivity, List<Activity>>> viewAndActivities =
      RxUtils.combineLatestPair(viewSubject, activities);

    addSubscription(viewAndActivities
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(va -> va.first.onItemsNext(va.second)));
  }
}
