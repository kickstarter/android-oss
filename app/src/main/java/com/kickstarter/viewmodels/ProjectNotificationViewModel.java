package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Notification;
import com.kickstarter.services.ApiClient;
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

  // OUTPUTS
  private final BehaviorSubject<Notification> notificationOutput = BehaviorSubject.create();
  public final Observable<Notification> notification() {
    return notificationOutput;
  }

  // ERRORS
  private final PublishSubject<Throwable> unableToSavePreferenceError = PublishSubject.create();
  public Observable<String> unableToSavePreferenceError() {
    return unableToSavePreferenceError
      .map(__ -> null);
  }

  public final ProjectNotificationViewModelInputs inputs = this;
  public final ProjectNotificationViewModelOutputs outputs = this;
  public final ProjectNotificationViewModelErrors errors = this;

  public ProjectNotificationViewModel(final @NonNull Notification notification, final @NonNull ApiClient client) {
    final BehaviorSubject<Notification> notificationInput = BehaviorSubject.create(notification);

    notificationInput
      .compose(Transformers.takePairWhen(checked))
      .switchMap(nc -> this.updateNotification(client, nc.first, nc.second))
      .subscribe(notificationInput);

    notificationInput
      .subscribe(this.notificationOutput);

    this.notificationOutput
      .compose(Transformers.takeWhen(unableToSavePreferenceError))
      .subscribe(this.notificationOutput::onNext);
  }

  private Observable<Notification> updateNotification(final @NonNull ApiClient client,
    final @NonNull Notification notification, final boolean checked) {
    return client.updateProjectNotifications(notification, checked)
      .compose(Transformers.pipeErrorsTo(unableToSavePreferenceError));
  }
}
