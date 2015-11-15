package com.kickstarter.presenters.errors;

import rx.Observable;

public interface ForgotPasswordPresenterErrors {
  Observable<String> resetError();
}
