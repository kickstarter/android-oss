package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Notification;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.activities.ManageNotificationActivity;
import com.kickstarter.viewmodels.errors.ManageNotificationsViewModelErrors;
import com.kickstarter.viewmodels.outputs.ManageNotificationsViewModelOutputs;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class ManageNotificationsViewModel extends ViewModel<ManageNotificationActivity> implements
  ManageNotificationsViewModelOutputs, ManageNotificationsViewModelErrors {
  protected @Inject ApiClientType client;

  // OUTPUTS
  private Observable<List<Notification>> notifications;
  public Observable<List<Notification>> notifications() {
    return notifications;
  }

  // ERRORS
  private final PublishSubject<Throwable> unableToFetchNotificationsError = PublishSubject.create();
  public Observable<String> unableToFetchNotificationsError() {
    return unableToFetchNotificationsError
      .map(__ -> null); // todo: correct error string
  }

  public final ManageNotificationsViewModelOutputs outputs = this;
  public final ManageNotificationsViewModelErrors errors = this;

  @Override
  public void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    notifications = client.fetchNotifications()
      .compose(Transformers.pipeErrorsTo(unableToFetchNotificationsError));
  }
}
