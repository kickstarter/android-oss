package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface FacebookConfirmationPresenterOutputs {
  Observable<Void> signupSuccess();
  Observable<Boolean> isFormSubmitting();
  Observable<Boolean> isFormValid();
}
