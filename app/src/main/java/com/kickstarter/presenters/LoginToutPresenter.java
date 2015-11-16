package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.presenters.errors.LoginToutPresenterErrors;
import com.kickstarter.presenters.inputs.LoginToutPresenterInputs;
import com.kickstarter.presenters.outputs.LoginToutPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.LoginToutActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class LoginToutPresenter extends Presenter<LoginToutActivity> implements LoginToutPresenterInputs,
  LoginToutPresenterOutputs, LoginToutPresenterErrors {
  // INPUTS
  private final PublishSubject<String> facebookAccessToken = PublishSubject.create();
  private final PublishSubject<String> reason = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> facebookLoginSuccess = PublishSubject.create();
  public final Observable<Void> facebookLoginSuccess() {
    return facebookLoginSuccess.asObservable();
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
  public final Observable<ErrorEnvelope.FacebookUser> confirmFacebookSignupError() {
   return loginError
     .filter(ErrorEnvelope::isConfirmFacebookSignupError)
     .map(__ -> null);
  }

  public final Observable<String> missingFacebookEmailError() {
    return loginError
      .filter(ErrorEnvelope::isMissingFacebookEmailError)
      .map(ErrorEnvelope::errorMessage);
  }

  public final Observable<String> facebookInvalidAccessTokenError() {
    return loginError
      .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
      .map(ErrorEnvelope::errorMessage);
  }

  public final Observable<Void> tfaChallenge() {
    return loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }

  @Inject CurrentUser currentUser;
  @Inject ApiClient client;
  @Inject Logout logout;

  public final LoginToutPresenterInputs inputs = this;
  public final LoginToutPresenterOutputs outputs = this;
  public final LoginToutPresenterErrors errors = this;

  public LoginToutPresenter() {
    addSubscription(facebookAccessToken
      .switchMap(this::loginWithFacebook)
      .subscribe(this::loginWithFacebookSuccess));
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
    addSubscription(reason.take(1).subscribe(koala::trackLoginRegisterTout));

    // Clear any instance of a logged in user.
    logout.execute();
  }

  @Override
  public void facebookAccessToken(@NonNull final String fbAccessToken) {
    facebookAccessToken.onNext(fbAccessToken);
  }

  private Observable<AccessTokenEnvelope> loginWithFacebook(@NonNull final String fbAccessToken) {
    return client.loginWithFacebook(fbAccessToken)
      .compose(Transformers.pipeApiErrorsTo(loginError));
  }

  public void loginWithFacebookSuccess(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    facebookLoginSuccess.onNext(null);
  }

  @Override
  public void reason(@Nullable final String r) {
    reason.onNext(r);
  }
}
