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
import com.kickstarter.ui.activities.ManageNotificationActivity;
import com.kickstarter.ui.adapters.ManageNotificationsAdapter;
import com.kickstarter.ui.viewholders.ManageNotificationsViewHolder;
import com.kickstarter.viewmodels.outputs.ManageNotificationsOutputs;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ManageNotificationsViewModel extends ViewModel<ManageNotificationActivity> implements
  ManageNotificationsOutputs, ManageNotificationsAdapter.Delegate {
  @Inject ApiClient client;

  // OUTPUTS
  // todo: we only want to emit the List once!
  private final BehaviorSubject<List<Notification>> projectNotifications = BehaviorSubject.create();
  public Observable<List<Notification>> projectNotifications() {
    return projectNotifications;
  }

  // ERRORS
  private final PublishSubject<Throwable> errors = PublishSubject.create();

  public final ManageNotificationsOutputs outputs = this;

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

  @Override
  public void switchClicked(final @NonNull ManageNotificationsViewHolder viewHolder,
    final @NonNull Notification notification, final boolean toggleValue) {

    final Observable<Notification> updatedNotification = client
      .updateProjectNotifications(notification.id(), toggleValue)
      .compose(Transformers.pipeErrorsTo(errors));

    projectNotifications
      .compose(Transformers.takeWhen(updatedNotification))
      .subscribe();
  }
}
