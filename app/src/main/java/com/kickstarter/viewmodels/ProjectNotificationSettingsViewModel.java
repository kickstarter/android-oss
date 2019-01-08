package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.activities.ProjectNotificationSettingsActivity;

import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

public interface ProjectNotificationSettingsViewModel {

  interface Outputs {
    Observable<List<ProjectNotification>> projectNotifications();

    Observable<Void> unableToFetchProjectNotificationsError();
  }

  final class ViewModel extends ActivityViewModel<ProjectNotificationSettingsActivity> implements Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final ApiClientType client = environment.apiClient();

      this.projectNotifications = client.fetchProjectNotifications()
        .compose(Transformers.pipeErrorsTo(this.unableToFetchProjectNotificationsError));
    }

    private Observable<List<ProjectNotification>> projectNotifications;
    private final PublishSubject<Throwable> unableToFetchProjectNotificationsError = PublishSubject.create();

    public final Outputs outputs = this;

    @Override public @NonNull Observable<List<ProjectNotification>> projectNotifications() {
      return this.projectNotifications;
    }
    @Override public @NonNull Observable<Void> unableToFetchProjectNotificationsError() {
      return this.unableToFetchProjectNotificationsError
        .map(__ -> null);
    }
  }
}
