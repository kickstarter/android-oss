package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface SignupViewModelOutputs {
  Observable<Void> signupSuccess();
  Observable<Boolean> formSubmitting();
  Observable<Boolean> formIsValid();
}
