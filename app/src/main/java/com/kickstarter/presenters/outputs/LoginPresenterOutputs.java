package com.kickstarter.presenters.outputs;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.Observable;

public interface LoginPresenterOutputs {
  Observable<Void> loginSuccess();
}
