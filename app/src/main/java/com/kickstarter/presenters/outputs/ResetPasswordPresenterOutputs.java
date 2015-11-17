package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface ResetPasswordPresenterOutputs {
  Observable<Void> resetSuccess();
  Observable<Boolean> isFormSubmitting();
  Observable<Boolean> isFormValid();
}
