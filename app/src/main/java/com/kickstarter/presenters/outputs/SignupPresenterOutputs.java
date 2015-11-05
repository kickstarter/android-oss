package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface SignupPresenterOutputs {
  boolean sendNewslettersDefault = true;
  Observable<Void> signupSuccess();
}
