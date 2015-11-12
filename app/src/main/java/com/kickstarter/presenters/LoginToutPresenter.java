package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.login.LoginResult;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public final class LoginToutPresenter extends Presenter<LoginToutActivity> implements LoginToutPresenterInputs,
  LoginToutPresenterOutputs, LoginToutPresenterErrors {
  // INPUTS
  private final PublishSubject<LoginResult> facebookLoginResult = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> facebookLoginSuccess = PublishSubject.create();
  public final Observable<Void> facebookLoginSuccess() {
    return facebookLoginSuccess.asObservable();
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
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

  public final LoginToutPresenterInputs inputs = this;
  public final LoginToutPresenterOutputs outputs = this;
  public final LoginToutPresenterErrors errors = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  public LoginToutPresenter() {
    addSubscription(facebookLoginResult
      .switchMap(this::submit)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::success));

    addSubscription(loginError.subscribe(errorEnvelope -> {
      Log.d("TEST", errorEnvelope.ksrCode());
    }));
  }

  @Override
  public void facebookLoginResult(@NonNull final LoginResult result) {
    facebookLoginResult.onNext(result);
  }

  private Observable<AccessTokenEnvelope> submit(@NonNull final LoginResult result) {
    return client.loginWithFacebook(result.getAccessToken().getToken())
      .compose(Transformers.pipeApiErrorsTo(loginError));
  }

  public void success(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    facebookLoginSuccess.onNext(null);
  }
}
