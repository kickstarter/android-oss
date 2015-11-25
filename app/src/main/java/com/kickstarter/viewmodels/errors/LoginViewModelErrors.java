package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface LoginViewModelErrors {
  Observable<String> invalidLoginError();
  Observable<String> genericLoginError();
  Observable<Void> tfaChallenge();
}
