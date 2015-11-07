package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface SignupPresenterOutputs {
  Observable<Void> signupSuccess();
  Observable<Boolean> formSubmitting();
  Observable<Boolean> formIsValid();
}
