package com.kickstarter.presenters.errors;

import rx.Observable;

public interface LoginPresenterErrors {
  Observable<String> invalidLoginError();
  Observable<String> genericLoginError();
  Observable<Void> tfaChallenge();
}
