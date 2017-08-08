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

    this.client = environment.apiClient();
    this.currentUser = environment.currentUser();

    final Observable<Pair<String, String>> emailAndPassword = this.email
      .compose(combineLatestPair(this.password));

    final Observable<Boolean> isValid = emailAndPassword
      .map(ep -> LoginViewModel.isValid(ep.first, ep.second));

    final Observable<String> emailFromIntent = intent()
      .map(i -> i.getStringExtra(IntentKey.EMAIL))
      .ofType(String.class)
      .compose(bindToLifecycle());

    emailFromIntent
      .compose(bindToLifecycle())
      .subscribe(this.prefillEmailFromPasswordReset);

    emailFromIntent
      .map(email -> Pair.create(true, email))
      .compose(bindToLifecycle())
      .subscribe(this.showResetPasswordSuccessDialog);

    this.resetPasswordConfirmationDialogDismissed
      .map(BooleanUtils::negate)
      .compose(combineLatestPair(emailFromIntent))
      .compose(bindToLifecycle())
      .subscribe(this.showResetPasswordSuccessDialog);

    isValid
      .compose(bindToLifecycle())
      .subscribe(this.setLoginButtonIsEnabled);

    emailAndPassword
      .compose(takeWhen(this.loginClick))
      .switchMap(ep -> submit(ep.first, ep.second))
      .compose(bindToLifecycle())
      .subscribe(this::success);

    this.loginSuccess
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackLoginSuccess());

    invalidLoginError()
      .mergeWith(genericLoginError())
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackLoginError());
  }

  private static boolean isValid(final @NonNull String email, final @NonNull String password) {
    return StringUtils.isEmail(email) && password.length() > 0;
  }

  private Observable<AccessTokenEnvelope> submit(final @NonNull String email, final @NonNull String password) {
    return this.client.login(email, password)
      .compose(Transformers.pipeApiErrorsTo(this.loginError))
      .compose(neverError());
  }

  private void success(final @NonNull AccessTokenEnvelope envelope) {
    this.currentUser.login(envelope.user(), envelope.accessToken());
    this.loginSuccess.onNext(null);
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
    this.email.onNext(s);
  }

  @Override public void loginClick() {
    this.loginClick.onNext(null);
  }

  @Override public void password(final @NonNull String s) {
    this.password.onNext(s);
  }

  @Override public void resetPasswordConfirmationDialogDismissed() {
    this.resetPasswordConfirmationDialogDismissed.onNext(true);
  }

  @Override public @NonNull Observable<Void> loginSuccess() {
    return this.loginSuccess.asObservable();
  }

  @Override public @NonNull Observable<String> prefillEmailFromPasswordReset() {
    return this.prefillEmailFromPasswordReset;
  }

  @Override public Observable<Boolean> setLoginButtonIsEnabled() {
    return this.setLoginButtonIsEnabled;
  }

  @Override public Observable<Pair<Boolean, String>> showResetPasswordSuccessDialog() {
    return this.showResetPasswordSuccessDialog;
  }

  @Override public Observable<String> invalidLoginError() {
    return this.loginError
      .filter(ErrorEnvelope::isInvalidLoginError)
      .map(ErrorEnvelope::errorMessage);
  }

  @Override public Observable<Void> tfaChallenge() {
    return this.loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }

  @Override public Observable<String> genericLoginError() {
    return this.loginError
      .filter(ErrorEnvelope::isGenericLoginError)
      .map(ErrorEnvelope::errorMessage);
  }
}
