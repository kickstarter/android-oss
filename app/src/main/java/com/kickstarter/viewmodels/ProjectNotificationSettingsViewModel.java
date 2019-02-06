package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.activities.ProjectNotificationSettingsActivity;

import java.util.List;

import androidx.annotation.NonNull;
import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.errors;
import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface ProjectNotificationSettingsViewModel {

  interface Outputs {
    Observable<List<ProjectNotification>> projectNotifications();

    Observable<Void> unableToFetchProjectNotificationsError();
  }

  final class ViewModel extends ActivityViewModel<ProjectNotificationSettingsActivity> implements Outputs {

    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();

      final Observable<Notification<List<ProjectNotification>>> projectNotificationsNotification =
        this.client
          .fetchProjectNotifications()
          .materialize();

      projectNotificationsNotification
        .compose(values())
        .compose(bindToLifecycle())
        .subscribe(this.projectNotifications::onNext);

      projectNotificationsNotification
        .compose(errors())
        .compose(bindToLifecycle())
        .subscribe(e -> this.unableToFetchProjectNotificationsError.onNext(null));
    }

    private final BehaviorSubject<List<ProjectNotification>> projectNotifications = BehaviorSubject.create();
    private final BehaviorSubject<Void> unableToFetchProjectNotificationsError = BehaviorSubject.create();

    public final Outputs outputs = this;

    @Override public @NonNull Observable<List<ProjectNotification>> projectNotifications() {
      return this.projectNotifications;
    }
    @Override public @NonNull Observable<Void> unableToFetchProjectNotificationsError() {
      return this.unableToFetchProjectNotificationsError;
    }
  }
}
