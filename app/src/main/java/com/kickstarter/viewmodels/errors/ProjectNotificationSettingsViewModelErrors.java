package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface ProjectNotificationSettingsViewModelErrors {
  Observable<Void> unableToFetchProjectNotificationsError();
}
