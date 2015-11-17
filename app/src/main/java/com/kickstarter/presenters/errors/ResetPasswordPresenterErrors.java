package com.kickstarter.presenters.errors;

import rx.Observable;

public interface ResetPasswordPresenterErrors {
  Observable<String> resetError();
}
