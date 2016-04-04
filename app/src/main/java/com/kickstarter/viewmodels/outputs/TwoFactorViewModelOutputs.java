package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface TwoFactorViewModelOutputs {
  Observable<Void> tfaSuccess();
  Observable<Boolean> formSubmitting();
  Observable<Boolean> formIsValid();
  Observable<Void> showResendCodeConfirmation();
}
