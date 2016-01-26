package com.kickstarter.viewmodels.errors;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.data.LoginReason;

import rx.Observable;

public interface LoginToutViewModelErrors {
  @NonNull Observable<Pair<ErrorEnvelope.FacebookUser, LoginReason>> confirmFacebookSignupError();
  Observable<String> missingFacebookEmailError();
  Observable<String> facebookInvalidAccessTokenError();
  Observable<String> facebookAuthorizationError();
  Observable<LoginReason> tfaChallenge();
}
