package com.kickstarter.viewmodels.outputs;

import android.support.annotation.NonNull;

import com.kickstarter.ui.data.LoginReason;

import rx.Observable;

public interface LoginToutViewModelOutputs {
  @NonNull Observable<LoginReason> startLogin();
//  @NonNull Observable<LoginReason> loginClickDefaultFlow();
  @NonNull Observable<Void> loginSuccessContextualFlow();
  @NonNull Observable<Void> loginSuccessDefaultFlow();
  @NonNull Observable<LoginReason> signupClickContextualFlow();
  @NonNull Observable<LoginReason> signupClickDefaultFlow();
}
