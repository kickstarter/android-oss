package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface FacebookConfirmationViewModelOutputs {
  Observable<Void> signupSuccess();
  Observable<Boolean> sendNewslettersIsChecked();
}
