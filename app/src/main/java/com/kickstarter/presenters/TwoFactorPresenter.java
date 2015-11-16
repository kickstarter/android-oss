package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.presenters.errors.TwoFactorPresenterErrors;
import com.kickstarter.presenters.inputs.TwoFactorPresenterInputs;
import com.kickstarter.presenters.outputs.TwoFactorPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.TwoFactorActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class TwoFactorPresenter extends Presenter<TwoFactorActivity> implements TwoFactorPresenterInputs,
  TwoFactorPresenterOutputs, TwoFactorPresenterErrors {

  protected final static class TfaData {
    @Nullable final String email;
    @Nullable final String fbAccessToken;
    final boolean isFacebookLogin;
    @Nullable final String password;
    @NonNull final String code;

    protected TfaData(@Nullable final String email, @Nullable final String fbAccessToken, final boolean isFacebookLogin,
      @Nullable final String password, @NonNull final String code) {
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
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<String> fbAccessToken = PublishSubject.create();
  private final PublishSubject<Boolean> isFacebookLogin = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<String> password = PublishSubject.create();
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

  @Inject CurrentUser currentUser;
  @Inject ApiClient client;

  public final TwoFactorPresenterInputs inputs = this;
  public final TwoFactorPresenterOutputs outputs = this;
  public final TwoFactorPresenterErrors errors = this;

  @Override
  public void email(@NonNull final String s) {
    email.onNext(s);
  }

  @Override
  public void fbAccessToken(@NonNull final String s) {
    fbAccessToken.onNext(s);
  }

  @Override
  public void isFacebookLogin(final boolean b) {
    isFacebookLogin.onNext(b);
  }

  @Override
  public void code(@NonNull final String s) {
    code.onNext(s);
  }

  @Override
  public void password(@NonNull final String s) {
    password.onNext(s);
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
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<TfaData> tfaData = Observable.combineLatest(email, fbAccessToken, isFacebookLogin, password, code,
      TfaData::new);
    final Observable<Pair<String, String>> emailAndPassword = email
      .compose(Transformers.combineLatestPair(password));

    addSubscription(tfaData
      .map(TfaData::isValid)
      .subscribe(this.formIsValid::onNext));

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
      .switchMap(ep -> resendCode(ep.first, ep.second))
      .subscribe()
    );
  }

  public Observable<AccessTokenEnvelope> loginWithFacebook(@NonNull final String fbAccessToken, @NonNull final String code) {
    return client.loginWithFacebook(fbAccessToken, code)
      .compose(Transformers.pipeApiErrorsTo(tfaError));
  }

  private void success(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    tfaSuccess.onNext(null);
  }

  private Observable<AccessTokenEnvelope> submit(@NonNull final TfaData data) {
    return client.login(data.email, data.password, data.code)
      .compose(Transformers.pipeApiErrorsTo(tfaError))
      .doOnSubscribe(() -> formSubmitting.onNext(true))
      .finallyDo(() -> formSubmitting.onNext(false));
  }

  private Observable<AccessTokenEnvelope> resendCode(@NonNull final String email, @NonNull final String password) {
    return client.login(email, password)
      .compose(Transformers.neverError());
  }
}
