package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface LoginToutPresenterOutputs {
  Observable<Void> facebookLoginSuccess();
}
