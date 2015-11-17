package com.kickstarter.presenters.errors;

import com.kickstarter.services.apiresponses.ErrorEnvelope;
import android.support.annotation.NonNull;

import com.facebook.FacebookException;

import rx.Observable;

public interface LoginToutPresenterErrors {
  Observable<ErrorEnvelope.FacebookUser> confirmFacebookSignupError();
  Observable<String> missingFacebookEmailError();
  Observable<String> facebookInvalidAccessTokenError();
  void facebookAuthorizationException(@NonNull final FacebookException e);
  Observable<Void> tfaChallenge();
}
