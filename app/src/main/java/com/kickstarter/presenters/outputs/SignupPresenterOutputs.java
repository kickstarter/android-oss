package com.kickstarter.presenters.outputs;

import rx.Observable;

public interface SignupPresenterOutputs {
  boolean SEND_NEWSLETTERS_DEFAULT = true;
  Observable<Void> signupSuccess();
  Observable<Boolean> formSubmitting();
}
