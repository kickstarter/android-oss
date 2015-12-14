package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Notification;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.ManageNotificationActivity;
import com.kickstarter.viewmodels.errors.ManageNotificationsViewModelErrors;
import com.kickstarter.viewmodels.outputs.ManageNotificationsViewModelOutputs;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class ManageNotificationsViewModel extends ViewModel<ManageNotificationActivity> implements
  ManageNotificationsViewModelErrors, ManageNotificationsViewModelOutputs {
  @Inject ApiClient client;

  // OUTPUTS
  private Observable<List<Notification>> notifications;
  public final Observable<List<Notification>> notifications() {
    return notifications;
  }

  // ERRORS
  private final PublishSubject<Throwable> unableToSavePreferenceError = PublishSubject.create();
  public Observable<String> unableToSavePreferenceError() {
    return unableToSavePreferenceError
      .map(__ -> null); // todo: correct error string
  }

  public final ManageNotificationsViewModelOutputs outputs = this;
  public final ManageNotificationsViewModelErrors errors = this;

  @Override
  public void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    this.notifications = client.fetchProjectNotifications()
      .compose(Transformers.pipeErrorsTo(unableToSavePreferenceError));

    notifications
      .window(2, 1)
      .flatMap(Observable::toList)
      .compose(Transformers.takeWhen(unableToSavePreferenceError))
      .map(ListUtils::first)
      .subscribe(__ -> notifications());
  }
}
