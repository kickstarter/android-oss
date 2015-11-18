package com.kickstarter.presenters.errors;

import rx.Observable;

public interface FacebookConfirmationPresenterErrors {
  Observable<String> signupError();
}
