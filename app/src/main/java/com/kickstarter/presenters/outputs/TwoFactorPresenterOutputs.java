package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface TwoFactorPresenterOutputs {
  Observable<Void> tfaSuccess();
  Observable<Boolean> formSubmitting();
  Observable<Boolean> formIsValid();
}
