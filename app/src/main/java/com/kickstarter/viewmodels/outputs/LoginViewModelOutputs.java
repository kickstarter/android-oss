package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface LoginViewModelOutputs {
  Observable<String> prefillEmail();
  Observable<Void> loginSuccess();
}
