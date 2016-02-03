package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface LoginToutViewModelErrors {
  /**
   * String to display when a user has failed to authenticate using Facebook.
   */
  Observable<String> facebookAuthorizationError();

  /**
   * String to display when the API was unable to create a new Facebook user.
   */
  Observable<String> facebookInvalidAccessTokenError();

  /**
   * String to display when the API could not retrieve an email for the Facebook user.
   */
  Observable<String> missingFacebookEmailError();

  /**
   * Emits when a user has successfully logged in using Facebook, but we require two-factor authentication
   * before allowing them to login.
   */
  Observable<Void> startTwoFactorChallenge();
}
