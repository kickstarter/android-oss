package com.kickstarter.viewmodels.errors;

import android.util.Pair;

import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.data.LoginReason;

import rx.Observable;

public interface LoginToutViewModelErrors {
  Observable<Pair<ErrorEnvelope.FacebookUser, LoginReason>> confirmFacebookSignupError();
  Observable<String> facebookAuthorizationError();
  Observable<String> facebookInvalidAccessTokenError();
  Observable<String> missingFacebookEmailError();
  Observable<LoginReason> startTwoFactorChallenge();
}
