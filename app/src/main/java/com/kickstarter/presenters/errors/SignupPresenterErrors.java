package com.kickstarter.presenters.errors;

import rx.Observable;

public interface SignupPresenterErrors {
  Observable<String> signupError();
}
