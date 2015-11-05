package com.kickstarter.presenters.errors;

import java.util.List;

import rx.Observable;

public interface SignupPresenterErrors {
  Observable<List<String>> signupError();
}
