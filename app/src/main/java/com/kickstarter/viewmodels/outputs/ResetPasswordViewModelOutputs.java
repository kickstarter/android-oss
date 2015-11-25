package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface ResetPasswordViewModelOutputs {
  Observable<Void> resetSuccess();
  Observable<Boolean> isFormSubmitting();
  Observable<Boolean> isFormValid();
}
