package com.kickstarter.viewmodels.outputs;

import rx.Observable;

public interface LoginToutViewModelOutputs {
  Observable<Void> facebookLoginSuccess();
}
