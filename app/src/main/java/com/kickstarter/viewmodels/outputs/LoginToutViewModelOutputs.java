package com.kickstarter.viewmodels.outputs;

import android.support.annotation.NonNull;

import rx.Observable;

public interface LoginToutViewModelOutputs {
  @NonNull Observable<Void> finishWithSuccessfulResult();
  @NonNull Observable<Void> startLogin();
  @NonNull Observable<Void> startSignup();
}
