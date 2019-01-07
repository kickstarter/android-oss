package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.viewholders.ProjectNotificationViewHolder;

import androidx.annotation.NonNull;
import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.errors;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface ProjectNotificationViewModel {
  interface Inputs {
    /** Call when the enable switch is clicked. */
    void enabledSwitchClick(boolean enabled);

    /** Call when a notification is bound to the viewholder. */
    void projectNotification(ProjectNotification projectNotification);
  }

  interface Outputs {
    /** Emits `True` if the enabled switch should be toggled on, `False` otherwise. */
    Observable<Boolean> enabledSwitch();

    /** Emits the project's name. */
    Observable<String> projectName();

    /**  Show an error indicating the notification cannot be saved. */
    Observable<Void> showUnableToSaveProjectNotificationError();
  }

  final class ViewModel extends ActivityViewModel<ProjectNotificationViewHolder> implements Inputs, Outputs {
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final ApiClientType client = environment.apiClient();

      // When the enable switch is clicked, update the project notification.
      final Observable<Notification<ProjectNotification>> updateNotification = this.projectNotification
        .compose(takePairWhen(this.enabledSwitchClick))
        .switchMap(ne ->
          client
            .updateProjectNotifications(ne.first, ne.second)
            .materialize()
        )
        .share();

      updateNotification
        .compose(values())
        .compose(bindToLifecycle())
        .subscribe(this.projectNotification::onNext);

      updateNotification
        .compose(errors())
        .compose(bindToLifecycle())
        .subscribe(__ -> this.showUnableToSaveProjectNotificationError.onNext(null));

      // Update the project name when a project notification emits.
      this.projectNotification
        .map(n -> n.project().name())
        .compose(bindToLifecycle())
        .subscribe(this.projectName::onNext);

      // Update the enabled switch when a project notification emits.
      this.projectNotification
        .map(n -> n.email() && n.mobile())
        .compose(bindToLifecycle())
        .subscribe(this.enabledSwitch::onNext);
    }

    private PublishSubject<Boolean> enabledSwitchClick = PublishSubject.create();
    private PublishSubject<ProjectNotification> projectNotification = PublishSubject.create();

    private BehaviorSubject<String> projectName = BehaviorSubject.create();
    private BehaviorSubject<Boolean> enabledSwitch = BehaviorSubject.create();

    private PublishSubject<Void> showUnableToSaveProjectNotificationError = PublishSubject.create();

    public Inputs inputs = this;
    public Outputs outputs = this;

    @Override public void enabledSwitchClick(final boolean enabled) {
      this.enabledSwitchClick.onNext(enabled);
    }
    @Override public void projectNotification(final @NonNull ProjectNotification projectNotification) {
      this.projectNotification.onNext(projectNotification);
    }

    @Override public @NonNull Observable<String> projectName() {
      return this.projectName;
    }
    @Override public @NonNull Observable<Boolean> enabledSwitch() {
      return this.enabledSwitch;
    }
    @Override public @NonNull Observable<Void> showUnableToSaveProjectNotificationError() {
      return this.showUnableToSaveProjectNotificationError;
    }
  }
}
