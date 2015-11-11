package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface LoginPresenterOutputs {
  Observable<Void> loginSuccess();
}
