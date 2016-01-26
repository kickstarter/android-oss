package com.kickstarter.viewmodels.outputs;

import android.support.annotation.NonNull;

import com.kickstarter.ui.data.LoginReason;

import rx.Observable;

public interface LoginToutViewModelOutputs {
  @NonNull Observable<Void> finishWithSuccessfulResult();
  @NonNull Observable<LoginReason> startLogin();
  @NonNull Observable<Void> startSignup();
}
