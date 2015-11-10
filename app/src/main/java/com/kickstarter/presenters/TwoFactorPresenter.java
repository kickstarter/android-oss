package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;

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
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public final class TwoFactorPresenter extends Presenter<TwoFactorActivity> implements TwoFactorPresenterInputs, TwoFactorPresenterOutputs, TwoFactorPresenterErrors {
  // INPUTS
  private final PublishSubject<String> code = PublishSubject.create();
  private final PublishSubject<View> loginClick = PublishSubject.create();
  private final PublishSubject<View> resendClick = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  public Observable<Void> loginSuccess() {
    return loginSuccess.asObservable();
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
  public void code(@NonNull final String s) {
    code.onNext(s);
  }

  @Override
  public void loginClick(@NonNull final View view) {
    loginClick.onNext(view);
  }

  @Override
  public void resendClick(@NonNull final View view) {
    resendClick.onNext(view);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<Boolean> isValid = code
      .map(TwoFactorPresenter::isValid);

    final Observable<Pair<TwoFactorActivity, String>> viewAndCode = viewSubject
      .compose(Transformers.combineLatestPair(code));

    final Observable<AccessTokenEnvelope> tokenEnvelope = viewAndCode
      .compose(Transformers.takeWhen(loginClick))
      .switchMap(vc -> submit(vc.first.email(), vc.first.password(), vc.second));

    addSubscription(viewSubject
        .compose(Transformers.combineLatestPair(isValid))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(viewAndValid -> viewAndValid.first.setLoginEnabled(viewAndValid.second))
    );

    addSubscription(tokenEnvelope
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::success)
    );

    addSubscription(viewSubject
        .compose(Transformers.takeWhen(resendClick))
        .switchMap(view -> resendCode(view.email(), view.password()))
        .observeOn(AndroidSchedulers.mainThread())
          // TODO: It might be a gotcha to have an empty subscription block, but I don't remember
          // why. We should investigate.
        .subscribe()
    );

    addSubscription(loginSuccess
        .subscribe(__ -> koala.trackLoginSuccess())
    );

    addSubscription(resendClick
      .subscribe(__ -> koala.trackTwoFactorResendCode())
    );

    addSubscription(tfaError
        .subscribe(__ -> koala.trackLoginError())
    );

    koala.trackTwoFactorAuthView();
  }

  private static boolean isValid(@NonNull final String code) {
    return code.length() > 0;
  }

  public void takeLoginClick() {
    loginClick.onNext(null);
  }

  public void takeResendClick() {
    resendClick.onNext(null);
  }

  private void success(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    loginSuccess.onNext(null);
  }

  private Observable<AccessTokenEnvelope> submit(@NonNull final String email, @NonNull final String password,
    @NonNull final String code) {
    return client.login(email, password, code)
      .compose(Transformers.pipeApiErrorsTo(tfaError));
  }

  private Observable<AccessTokenEnvelope> resendCode(@NonNull final String email, @NonNull final String password) {
    return client.login(email, password)
      .compose(Transformers.neverError());
  }
}
