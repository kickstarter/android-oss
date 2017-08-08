package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.TwoFactorActivity;
import com.kickstarter.viewmodels.errors.TwoFactorViewModelErrors;
import com.kickstarter.viewmodels.inputs.TwoFactorViewModelInputs;
import com.kickstarter.viewmodels.outputs.TwoFactorViewModelOutputs;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class TwoFactorViewModel extends ActivityViewModel<TwoFactorActivity> implements TwoFactorViewModelInputs,
  TwoFactorViewModelOutputs, TwoFactorViewModelErrors {

  protected final static class TfaData {
    final @NonNull String email;
    final @NonNull String fbAccessToken;
    final boolean isFacebookLogin;
    final @NonNull String password;

    protected TfaData(final @NonNull String email, final @NonNull String fbAccessToken, final boolean isFacebookLogin,
      final @NonNull String password) {
      this.email = email;
      this.fbAccessToken = fbAccessToken;
      this.isFacebookLogin = isFacebookLogin;
      this.password = password;
    }
  }

  // INPUTS
  private final PublishSubject<String> code = PublishSubject.create();
  @Override
  public void code(@NonNull final String s) {
    this.code.onNext(s);
  }
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  @Override
  public void loginClick() {
    this.loginClick.onNext(null);
  }
  private final PublishSubject<Void> resendClick = PublishSubject.create();
  @Override
  public void resendClick() {
    this.resendClick.onNext(null);
  }

  // OUTPUTS
  private final PublishSubject<Boolean> formSubmitting = PublishSubject.create();
  public Observable<Boolean> formSubmitting() {
    return this.formSubmitting.asObservable();
  }
  private final PublishSubject<Boolean> formIsValid = PublishSubject.create();
  public Observable<Boolean> formIsValid() {
    return this.formIsValid.asObservable();
  }
  private final PublishSubject<Void> tfaSuccess = PublishSubject.create();
  public Observable<Void> tfaSuccess() {
    return this.tfaSuccess.asObservable();
  }
  private final PublishSubject<Void> showResendCodeConfirmation = PublishSubject.create();
  public Observable<Void> showResendCodeConfirmation() {
    return this.showResendCodeConfirmation.asObservable();
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> tfaError = PublishSubject.create();
  public Observable<Void> tfaCodeMismatchError() {
    return this.tfaError
      .filter(ErrorEnvelope::isTfaFailedError)
      .map(__ -> null);
  }
  public Observable<Void> genericTfaError() {
    return this.tfaError
      .filter(env -> !env.isTfaFailedError())
      .map(__ -> null);
  }

  private final ApiClientType client;
  private final CurrentUserType currentUser;

  public final TwoFactorViewModelInputs inputs = this;
  public final TwoFactorViewModelOutputs outputs = this;
  public final TwoFactorViewModelErrors errors = this;

  public TwoFactorViewModel(final @NonNull Environment environment) {
    super(environment);

    this.currentUser = environment.currentUser();
    this.client = environment.apiClient();

    final Observable<String> email = intent()
      .map(i -> i.getStringExtra(IntentKey.EMAIL));
    final Observable<String> fbAccessToken = intent()
      .map(i -> i.getStringExtra(IntentKey.FACEBOOK_TOKEN));
    final Observable<Boolean> isFacebookLogin = intent()
      .map(i -> i.getBooleanExtra(IntentKey.FACEBOOK_LOGIN, false));
    final Observable<String> password= intent()
      .map(i -> i.getStringExtra(IntentKey.PASSWORD));

    final Observable<TfaData> tfaData = Observable.combineLatest(
      email, fbAccessToken, isFacebookLogin, password, TfaData::new
    );

    this.code
      .map(TwoFactorViewModel::isCodeValid)
      .compose(bindToLifecycle())
      .subscribe(this.formIsValid);

    this.code
      .compose(Transformers.combineLatestPair(tfaData))
      .compose(Transformers.takeWhen(this.loginClick))
      .filter(cd -> !cd.second.isFacebookLogin)
      .switchMap(cd -> this.login(cd.first, cd.second.email, cd.second.password))
      .compose(bindToLifecycle())
      .subscribe(this::success);

    this.code
      .compose(Transformers.combineLatestPair(tfaData))
      .compose(Transformers.takeWhen(this.loginClick))
      .filter(cd -> cd.second.isFacebookLogin)
      .switchMap(cd -> this.loginWithFacebook(cd.first, cd.second.fbAccessToken))
      .compose(bindToLifecycle())
      .subscribe(this::success);

    tfaData
      .compose(Transformers.takeWhen(this.resendClick))
      .filter(d -> !d.isFacebookLogin)
      .flatMap(d -> resendCode(d.email, d.password))
      .compose(bindToLifecycle())
      .subscribe();

    tfaData
      .compose(Transformers.takeWhen(this.resendClick))
      .filter(d -> d.isFacebookLogin)
      .flatMap(d -> resendCodeWithFacebook(d.fbAccessToken))
      .compose(bindToLifecycle())
      .subscribe();

    this.tfaSuccess
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackLoginSuccess());

    this.resendClick
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackTwoFactorResendCode());

    this.tfaError
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackLoginError());

    this.koala.trackTwoFactorAuthView();
  }

  private void success(final @NonNull AccessTokenEnvelope envelope) {
    this.currentUser.login(envelope.user(), envelope.accessToken());
    this.tfaSuccess.onNext(null);
  }

  private Observable<AccessTokenEnvelope> login(final @NonNull String code, final @NonNull String email,
    final @NonNull String password) {
    return this.client.login(email, password, code)
      .compose(Transformers.pipeApiErrorsTo(this.tfaError))
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> this.formSubmitting.onNext(true))
      .doAfterTerminate(() -> this.formSubmitting.onNext(false));
  }

  public Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String code, final @NonNull String fbAccessToken) {
    return this.client.loginWithFacebook(fbAccessToken, code)
      .compose(Transformers.pipeApiErrorsTo(this.tfaError))
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> this.formSubmitting.onNext(true))
      .doAfterTerminate(() -> this.formSubmitting.onNext(false));
  }

  private Observable<AccessTokenEnvelope> resendCode(final @NonNull String email, final @NonNull String password) {
    return this.client.login(email, password)
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> this.showResendCodeConfirmation.onNext(null));
  }

  private Observable<AccessTokenEnvelope> resendCodeWithFacebook(final @NonNull String fbAccessToken) {
    return this.client.loginWithFacebook(fbAccessToken)
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> this.showResendCodeConfirmation.onNext(null));
  }

  private static boolean isCodeValid(final String code) {
    return code != null && code.length() > 0;
  }
}
