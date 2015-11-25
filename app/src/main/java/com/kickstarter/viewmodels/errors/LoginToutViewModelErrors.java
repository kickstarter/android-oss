package com.kickstarter.viewmodels.errors;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.Observable;

public interface LoginToutViewModelErrors {
  Observable<ErrorEnvelope.FacebookUser> confirmFacebookSignupError();
  Observable<String> missingFacebookEmailError();
  Observable<String> facebookInvalidAccessTokenError();
  Observable<String> facebookAuthorizationError();
  Observable<Void> tfaChallenge();
}
