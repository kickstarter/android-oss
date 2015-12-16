package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface ManageNotificationsViewModelErrors {
  Observable<String> unableToFetchNotificationsError();
}
