package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
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
  private final BehaviorSubject<Notification> notification;
  public final Observable<Notification> notification() {
    return notification;
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
    this.notification = BehaviorSubject.create(notification);

    this.notification
      .compose(Transformers.takePairWhen(checked))
      .switchMap(nc -> client.updateProjectNotifications(nc.first, nc.second))
      .compose(Transformers.pipeErrorsTo(unableToSavePreferenceError))
      .subscribe(this.notification);

    this.notification
      .window(2, 1)
      .flatMap(Observable::toList)
      .compose(Transformers.takeWhen(unableToSavePreferenceError))
      .map(ListUtils::first)
      .subscribe(this.notification);

    addSubscription(checked.subscribe(koala::trackNewsletterToggle));
  }
}
