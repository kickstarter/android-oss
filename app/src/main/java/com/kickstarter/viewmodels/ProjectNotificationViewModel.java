package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Notification;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.viewholders.ProjectNotificationViewHolder;
import com.kickstarter.viewmodels.errors.ProjectNotificationViewModelErrors;
import com.kickstarter.viewmodels.inputs.ProjectNotificationViewModelInputs;
import com.kickstarter.viewmodels.outputs.ProjectNotificationViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class ProjectNotificationViewModel extends ViewModel<ProjectNotificationViewHolder> implements
  ProjectNotificationViewModelInputs, ProjectNotificationViewModelOutputs, ProjectNotificationViewModelErrors {

  // INPUTS
  private final PublishSubject<Boolean> checked = PublishSubject.create();
  public final void switchClick(boolean checked) {
    this.checked.onNext(checked);
  }
  final BehaviorSubject<Notification> notificationInput;

  // OUTPUTS
  private final BehaviorSubject<Notification> notificationOutput = BehaviorSubject.create();
  public final Observable<Notification> notification() {
    return notificationOutput;
  }
  private final PublishSubject<Void> updateSuccess = PublishSubject.create();
  public Observable<Void> updateSuccess() {
    return updateSuccess;
  }

  // ERRORS
  private final PublishSubject<Throwable> unableToSavePreferenceError = PublishSubject.create();
  public Observable<String> unableToSavePreferenceError() {
    return unableToSavePreferenceError
      .takeUntil(updateSuccess)
      .map(__ -> null);
  }

  public final ProjectNotificationViewModelInputs inputs = this;
  public final ProjectNotificationViewModelOutputs outputs = this;
  public final ProjectNotificationViewModelErrors errors = this;

  public ProjectNotificationViewModel(final @NonNull Notification notification, final @NonNull ApiClientType client) {
    notificationInput = BehaviorSubject.create(notification);

    notificationInput
      .compose(Transformers.takePairWhen(checked))
      .switchMap(nc -> updateNotification(client, nc.first, nc.second))
      .compose(bindToLifecycle())
      .subscribe(this::success);

    notificationInput
      .compose(bindToLifecycle())
      .subscribe(this.notificationOutput);

    this.notificationOutput
      .compose(Transformers.takeWhen(unableToSavePreferenceError))
      .compose(bindToLifecycle())
      .subscribe(this.notificationOutput::onNext);
  }

  private void success(final @NonNull Notification notification) {
    notificationInput.onNext(notification);
    this.updateSuccess.onNext(null);
  }

  private Observable<Notification> updateNotification(final @NonNull ApiClientType client,
    final @NonNull Notification notification, final boolean checked) {
    return client.updateNotifications(notification, checked)
      .compose(Transformers.pipeErrorsTo(unableToSavePreferenceError));
  }
}
