package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface ProjectNotificationViewModelErrors {
  Observable<String> unableToSavePreferenceError();
}
