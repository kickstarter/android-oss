package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.BooleanUtils;
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
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public final class LoginViewModel extends ActivityViewModel<LoginActivity> implements LoginViewModelInputs,
  LoginViewModelOutputs, LoginViewModelErrors {
  private final ApiClientType client;
  private final CurrentUserType currentUser;

  public LoginViewModel(final @NonNull Environment environment) {
    super(environment);

    client = environment.apiClient();
    currentUser = environment.currentUser();

    final Observable<Pair<String, String>> emailAndPassword = email
      .compose(combineLatestPair(password));

    final Observable<Boolean> isValid = emailAndPassword
      .map(ep -> LoginViewModel.isValid(ep.first, ep.second));

    final Observable<String> emailFromIntent = intent()
      .map(i -> i.getStringExtra(IntentKey.EMAIL))
      .ofType(String.class)
      .compose(bindToLifecycle());

    emailFromIntent
      .compose(bindToLifecycle())
      .subscribe(prefillEmailFromPasswordReset);

    emailFromIntent
      .map(email -> Pair.create(true, email))
      .compose(bindToLifecycle())
      .subscribe(showResetPasswordSuccessDialog);

    resetPasswordConfirmationDialogDismissed
      .map(BooleanUtils::negate)
      .compose(combineLatestPair(emailFromIntent))
      .compose(bindToLifecycle())
      .subscribe(showResetPasswordSuccessDialog);

    isValid
      .compose(bindToLifecycle())
      .subscribe(setLoginButtonIsEnabled);

    emailAndPassword
      .compose(takeWhen(loginClick))
      .switchMap(ep -> submit(ep.first, ep.second))
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
      .compose(neverError());
  }

  private void success(final @NonNull AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    loginSuccess.onNext(null);
  }

  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<View> loginClick = PublishSubject.create();
  private final PublishSubject<String> password = PublishSubject.create();
  private final PublishSubject<Boolean> resetPasswordConfirmationDialogDismissed = PublishSubject.create();

  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final BehaviorSubject<String> prefillEmailFromPasswordReset = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> setLoginButtonIsEnabled = BehaviorSubject.create();
  private final BehaviorSubject<Pair<Boolean, String>> showResetPasswordSuccessDialog = BehaviorSubject.create();

  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();

  public final LoginViewModelInputs inputs = this;
  public final LoginViewModelOutputs outputs = this;
  public final LoginViewModelErrors errors = this;

  @Override public void email(final @NonNull String s) {
    email.onNext(s);
  }

  @Override public void loginClick() {
    loginClick.onNext(null);
  }

  @Override public void password(final @NonNull String s) {
    password.onNext(s);
  }

  @Override public void resetPasswordConfirmationDialogDismissed() {
    resetPasswordConfirmationDialogDismissed.onNext(true);
  }

  @Override public @NonNull Observable<Void> loginSuccess() {
    return loginSuccess.asObservable();
  }

  @Override public @NonNull Observable<String> prefillEmailFromPasswordReset() {
    return prefillEmailFromPasswordReset;
  }

  @Override public Observable<Boolean> setLoginButtonIsEnabled() {
    return setLoginButtonIsEnabled;
  }

  @Override public Observable<Pair<Boolean, String>> showResetPasswordSuccessDialog() {
    return showResetPasswordSuccessDialog;
  }

  @Override public Observable<String> invalidLoginError() {
    return loginError
      .filter(ErrorEnvelope::isInvalidLoginError)
      .map(ErrorEnvelope::errorMessage);
  }

  @Override public Observable<Void> tfaChallenge() {
    return loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }

  @Override public Observable<String> genericLoginError() {
    return loginError
      .filter(ErrorEnvelope::isGenericLoginError)
      .map(ErrorEnvelope::errorMessage);
  }
}
