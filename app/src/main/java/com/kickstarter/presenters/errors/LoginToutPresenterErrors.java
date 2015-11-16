package com.kickstarter.presenters.errors;

import com.kickstarter.services.apiresponses.ErrorEnvelope;

import rx.Observable;

public interface LoginToutPresenterErrors {
  Observable<ErrorEnvelope.FacebookUser> confirmFacebookSignupError();
  Observable<String> missingFacebookEmailError();
  Observable<String> facebookInvalidAccessTokenError();
  Observable<Void> tfaChallenge();
}
