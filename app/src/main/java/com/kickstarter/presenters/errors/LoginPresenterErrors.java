package com.kickstarter.presenters.errors;

import rx.Observable;

public interface LoginPresenterErrors {
  Observable<Void> invalidLoginError();
  Observable<Void> genericLoginError();
  Observable<Void> tfaChallenge();
}
