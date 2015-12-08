package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Notification;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.ManageProjectNotificationActivity;
import com.kickstarter.viewmodels.outputs.ManageProjectNotificationsOutputs;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class ManageProjectNotificationsViewModel extends ViewModel<ManageProjectNotificationActivity> implements
  ManageProjectNotificationsOutputs {
  @Inject ApiClient client;

  // OUTPUTS
  private final BehaviorSubject<List<Notification>> projectNotifications = BehaviorSubject.create();
  public Observable<List<Notification>> projectNotifications() {
    return projectNotifications;
  }

  public final ManageProjectNotificationsOutputs outputs = this;

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    view
      .compose(Transformers.combineLatestPair(initialNotifications()))
      .flatMap(__ -> initialNotifications())
      .subscribe(projectNotifications);
  }

  private Observable<List<Notification>> initialNotifications() {
    return client.fetchProjectNotifications()
      .compose(Transformers.neverError());
  }
}
