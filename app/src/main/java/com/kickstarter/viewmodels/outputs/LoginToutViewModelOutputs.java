package com.kickstarter.viewmodels.outputs;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.Observable;

public interface LoginToutViewModelOutputs {
  /**
   * Emits when a user has successfully logged in; the login flow should be finished with a result indicating success.
   */
  @NonNull Observable<Void> finishWithSuccessfulResult();

  /**
   * Emits when the login activity should be started.
   */
  @NonNull Observable<Void> startLogin();

  /**
   * Emits when the signup activity should be started.
   */
  @NonNull Observable<Void> startSignup();

  /**
   * Emits a Facebook user and an access token string to be used to confirm facebook signup
   */
  @NonNull Observable<Pair<ErrorEnvelope.FacebookUser, String>> startConfirmFacebookSignup();
}
