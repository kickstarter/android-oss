package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface ProjectNotificationViewModelErrors {
  /**
   * Show an error indicating the notification cannot be saved.
   */
  Observable<Void> showUnableToSaveProjectNotificationError();
}
