package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.Observable;

public interface LoginToutViewModelOutputs {
  /**
   * Emits when a user has successfully logged in; the login flow should finish with a result indicating success.
   */
  Observable<Void> finishWithSuccessfulResult();

  /**
   * Emits when a user has failed to authenticate using Facebook.
   */
  Observable<String> showFacebookAuthorizationErrorDialog();

  /**
   * Emits when the API was unable to create a new Facebook user.
   */
  Observable<String> showFacebookInvalidAccessTokenErrorToast();

  /**
   * Emits when the API could not retrieve an email for the Facebook user.
   */
  Observable<String> showMissingFacebookEmailErrorToast();

  /**
   * Emits when a login attempt is unauthorized.
   */
  Observable<String> showUnauthorizedErrorDialog();

  /**
   * Emits a Facebook user and an access token string to confirm Facebook signup.
   */
  Observable<Pair<ErrorEnvelope.FacebookUser, String>> startFacebookConfirmationActivity();

  /**
   * Emits when the login activity should be started.
   */
  Observable<Void> startLoginActivity();

  /**
   * Emits when the signup activity should be started.
   */
  Observable<Void> startSignupActivity();

  /**
   * Emits when a user has successfully logged in using Facebook, but has require two-factor authentication enabled.
   */
  Observable<Void> startTwoFactorChallenge();
}
