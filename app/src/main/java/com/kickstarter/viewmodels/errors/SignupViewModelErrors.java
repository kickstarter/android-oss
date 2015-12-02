package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface SignupViewModelErrors {
  Observable<String> signupError();
}
