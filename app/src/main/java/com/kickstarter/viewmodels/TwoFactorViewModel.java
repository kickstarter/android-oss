package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.TwoFactorActivity;
import com.kickstarter.viewmodels.errors.TwoFactorViewModelErrors;
import com.kickstarter.viewmodels.inputs.TwoFactorViewModelInputs;
import com.kickstarter.viewmodels.outputs.TwoFactorViewModelOutputs;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class TwoFactorViewModel extends ViewModel<TwoFactorActivity> implements TwoFactorViewModelInputs,
  TwoFactorViewModelOutputs, TwoFactorViewModelErrors {

  protected final static class TfaData {
    final @Nullable String email;
    final @Nullable String fbAccessToken;
    final boolean isFacebookLogin;
    final @Nullable String password;
    final @NonNull String code;

    protected TfaData(final @Nullable String email, final @Nullable String fbAccessToken, final boolean isFacebookLogin,
      final @Nullable String password, final @NonNull String code) {
      this.email = email;
      this.fbAccessToken = fbAccessToken;
      this.isFacebookLogin = isFacebookLogin;
      this.password = password;
      this.code = code;
    }

    protected boolean isValid() {
      return code.length() > 0;
    }
  }

  // INPUTS
  private final PublishSubject<String> code = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<Void> resendClick = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Boolean> formSubmitting = PublishSubject.create();
  public final Observable<Boolean> formSubmitting() {
    return formSubmitting.asObservable();
  }
  private final PublishSubject<Boolean> formIsValid = PublishSubject.create();
  public final Observable<Boolean> formIsValid() {
    return formIsValid.asObservable();
  }
  private final PublishSubject<Void> tfaSuccess = PublishSubject.create();
  public final Observable<Void> tfaSuccess() {
    return tfaSuccess.asObservable();
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> tfaError = PublishSubject.create();
  public Observable<String> tfaCodeMismatchError() {
    return tfaError
      .filter(ErrorEnvelope::isTfaFailedError)
      .map(ErrorEnvelope::errorMessage);
  }
  public Observable<Void> genericTfaError() {
    return tfaError
      .filter(env -> !env.isTfaFailedError())
      .map(__ -> null);
  }

  protected @Inject CurrentUser currentUser;
  protected @Inject ApiClientType client;

  public final TwoFactorViewModelInputs inputs = this;
  public final TwoFactorViewModelOutputs outputs = this;
  public final TwoFactorViewModelErrors errors = this;

  @Override
  public void code(@NonNull final String s) {
    code.onNext(s);
  }

  @Override
  public void loginClick() {
    loginClick.onNext(null);
  }

  @Override
  public void resendClick() {
    resendClick.onNext(null);
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<String> email = intent
      .map(i -> i.getStringExtra(IntentKey.EMAIL));
    final Observable<String> fbAccessToken = intent
      .map(i -> i.getStringExtra(IntentKey.FACEBOOK_TOKEN));
    final Observable<Boolean> isFacebookLogin = intent
      .map(i -> i.getBooleanExtra(IntentKey.FACEBOOK_LOGIN, false));
    final Observable<String> password= intent
      .map(i -> i.getStringExtra(IntentKey.PASSWORD));

    final Observable<TfaData> tfaData = Observable.combineLatest(email, fbAccessToken, isFacebookLogin, password, code,
      TfaData::new);

    final Observable<Pair<String, String>> emailAndPassword = email
      .compose(Transformers.combineLatestPair(password));

    addSubscription(tfaData
      .map(TfaData::isValid)
      .subscribe(formIsValid));

    addSubscription(tfaData
      .compose(Transformers.takeWhen(loginClick))
      .filter(data -> !data.isFacebookLogin)
      .flatMap(this::submit)
      .subscribe(this::success));

    addSubscription(tfaData
      .compose(Transformers.takeWhen(loginClick))
      .filter(data -> data.isFacebookLogin)
      .flatMap(data -> loginWithFacebook(data.fbAccessToken, data.code))
      .subscribe(this::success));

    addSubscription(emailAndPassword
      .compose(Transformers.takeWhen(resendClick))
      .filter(ep -> ep.first != null)
      .switchMap(ep -> resendCode(ep.first, ep.second))
      .subscribe());

    addSubscription(fbAccessToken
      .compose(Transformers.takeWhen(resendClick))
      .filter(token -> token != null)
      .switchMap(this::resendCodeFbLogin)
      .subscribe());

    addSubscription(tfaSuccess.subscribe(__ -> koala.trackLoginSuccess()));

    addSubscription(resendClick.subscribe(__ -> koala.trackTwoFactorResendCode()));

    addSubscription(tfaError.subscribe(__ -> koala.trackLoginError()));

    koala.trackTwoFactorAuthView();
  }

  public Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String fbAccessToken, final @NonNull String code) {
    return client.loginWithFacebook(fbAccessToken, code)
      .compose(Transformers.pipeApiErrorsTo(tfaError))
      .compose(Transformers.neverError());
  }

  private void success(final @NonNull AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    tfaSuccess.onNext(null);
  }

  private Observable<AccessTokenEnvelope> submit(final @NonNull TfaData data) {
    return client.login(data.email, data.password, data.code)
      .compose(Transformers.pipeApiErrorsTo(tfaError))
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> formSubmitting.onNext(true))
      .finallyDo(() -> formSubmitting.onNext(false));
  }

  private Observable<AccessTokenEnvelope> resendCode(final @NonNull String email, final @NonNull String password) {
    return client.login(email, password)
      .compose(Transformers.neverError());
  }

  private Observable<AccessTokenEnvelope> resendCodeFbLogin(final @NonNull String fbAccessToken) {
    return client.loginWithFacebook(fbAccessToken)
      .compose(Transformers.neverError());
  }
}
