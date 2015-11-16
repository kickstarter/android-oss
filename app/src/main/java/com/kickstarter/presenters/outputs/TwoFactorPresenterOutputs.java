package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface TwoFactorPresenterOutputs {
  Observable<Void> loginSuccess();
}
