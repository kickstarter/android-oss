package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;

import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.LoginActivity;
import com.kickstarter.viewmodels.errors.LoginViewModelErrors;
import com.kickstarter.viewmodels.inputs.LoginViewModelInputs;
import com.kickstarter.viewmodels.outputs.LoginViewModelOutputs;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class LoginViewModel extends ActivityViewModel<LoginActivity> implements LoginViewModelInputs, LoginViewModelOutputs, LoginViewModelErrors {
  // INPUTS
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<View> loginClick = PublishSubject.create();
  private final PublishSubject<String> password = PublishSubject.create();

  // OUTPUTS
  private final BehaviorSubject<String> prefillEmailFromPasswordReset = BehaviorSubject.create();
  public @NonNull Observable<String> prefillEmailFromPasswordReset() {
    return prefillEmailFromPasswordReset;
  }

  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  public @NonNull Observable<Void> loginSuccess() {
    return loginSuccess.asObservable();
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
  public Observable<String> invalidLoginError() {
    return loginError
      .filter(ErrorEnvelope::isInvalidLoginError)
      .map(ErrorEnvelope::errorMessage);
  }
  public Observable<Void> tfaChallenge() {
    return loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }

  public Observable<String> genericLoginError() {
    return loginError
      .filter(ErrorEnvelope::isGenericLoginError)
      .map(ErrorEnvelope::errorMessage);
  }

  private final ApiClientType client;
  private final CurrentUserType currentUser;

  public final LoginViewModelInputs inputs = this;
  public final LoginViewModelOutputs outputs = this;
  public final LoginViewModelErrors errors = this;

  @Override
  public void email(final @NonNull String s) {
    email.onNext(s);
  }

  @Override
  public void loginClick() {
    loginClick.onNext(null);
  }

  @Override
  public void password(final @NonNull String s) {
    password.onNext(s);
  }

  public LoginViewModel(final @NonNull Environment environment) {
    super(environment);

    client = environment.apiClient();
    currentUser = environment.currentUser();
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    final Observable<Pair<String, String>> emailAndPassword = email
      .compose(Transformers.combineLatestPair(password));

    final Observable<Boolean> isValid = emailAndPassword
      .map(ep -> LoginViewModel.isValid(ep.first, ep.second));

    intent()
      .map(i -> i.getStringExtra(IntentKey.EMAIL))
      .ofType(String.class)
      .compose(bindToLifecycle())
      .subscribe(prefillEmailFromPasswordReset::onNext);

    view()
      .compose(Transformers.combineLatestPair(isValid))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(viewAndValid -> viewAndValid.first.setFormEnabled(viewAndValid.second));

    emailAndPassword
      .compose(Transformers.takeWhen(loginClick))
      .switchMap(ep -> submit(ep.first, ep.second))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(this::success);

    loginSuccess
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackLoginSuccess());

    invalidLoginError()
      .mergeWith(genericLoginError())
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackLoginError());
  }

  private static boolean isValid(final @NonNull String email, final @NonNull String password) {
    return StringUtils.isEmail(email) && password.length() > 0;
  }

  private Observable<AccessTokenEnvelope> submit(final @NonNull String email, final @NonNull String password) {
    return client.login(email, password)
      .compose(Transformers.pipeApiErrorsTo(loginError))
      .compose(Transformers.neverError());
  }

  private void success(final @NonNull AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    loginSuccess.onNext(null);
  }
}
