package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface FacebookConfirmationViewModelOutputs {
  Observable<String> prefillEmail();
  Observable<Void> signupSuccess();
  Observable<Boolean> sendNewslettersIsChecked();
}
