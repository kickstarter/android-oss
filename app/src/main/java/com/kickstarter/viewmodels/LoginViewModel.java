package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

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

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface LoginViewModel {

  // TODO: 10/4/17 finish adding javadoc
  interface Inputs {

    /** Call when the back or close button has been clicked. */
    void backOrCloseButtonClicked();

    /** Call when the email edit text changes. */
    void emailEditTextChanged(String email);

    /** Call when the log in button has been clicked. */
    void logInButtonClicked();

    /** Call when the password edit text changes. */
    void passwordEditTextChanged(String password);

    void resetPasswordConfirmationDialogDismissed();

    /** Call with the toolbar's title text. */
    void toolbarTitleText(String title);
  }

  interface Outputs {
    Observable<String> genericLoginError();

    Observable<String> invalidLoginError();

    /** Emits a boolean that determines if the log in button is enabled. */
    Observable<Boolean> logInButtonIsEnabled();

    Observable<Void> loginSuccess();

    Observable<String> preFillEmailFromPasswordReset();

    Observable<Pair<Boolean, String>> showResetPasswordSuccessDialog();

    Observable<Void> tfaChallenge();
  }

  final class ViewModel extends ActivityViewModel<LoginActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<Pair<String, String>> emailAndPassword = this.emailEditTextChanged
        .compose(combineLatestPair(this.passwordEditTextChanged));

      final Observable<Boolean> isValid = emailAndPassword
        .map(ep -> isValid(ep.first, ep.second));

      final Observable<String> emailFromIntent = intent()
        .map(i -> i.getStringExtra(IntentKey.EMAIL))
        .ofType(String.class)
        .compose(bindToLifecycle());

      emailFromIntent
        .compose(bindToLifecycle())
        .subscribe(this.preFillEmailFromPasswordReset);

      emailFromIntent
        .map(e -> Pair.create(true, e))
        .compose(bindToLifecycle())
        .subscribe(this.showResetPasswordSuccessDialog);

      this.resetPasswordConfirmationDialogDismissed
        .map(BooleanUtils::negate)
        .compose(combineLatestPair(emailFromIntent))
        .compose(bindToLifecycle())
        .subscribe(this.showResetPasswordSuccessDialog);

      isValid
        .compose(bindToLifecycle())
        .subscribe(this.logInButtonIsEnabled);

      emailAndPassword
        .compose(takeWhen(this.logInButtonClicked))
        .switchMap(ep -> submit(ep.first, ep.second))
        .compose(bindToLifecycle())
        .subscribe(this::success);

      this.loginSuccess
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackLoginSuccess());

      this.genericLoginError = this.loginError
          .filter(ErrorEnvelope::isGenericLoginError)
          .map(ErrorEnvelope::errorMessage);

      this.invalidloginError = this.loginError
        .filter(ErrorEnvelope::isInvalidLoginError)
        .map(ErrorEnvelope::errorMessage);

      invalidloginError
        .mergeWith(genericLoginError)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackLoginError());

      tfaChallenge = this.loginError
        .filter(ErrorEnvelope::isTfaRequiredError)
        .map(__ -> null);
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

    private final PublishSubject<Void> backOrCloseButtonClicked = PublishSubject.create();
    private final PublishSubject<String> emailEditTextChanged = PublishSubject.create();
    private final PublishSubject<Void> logInButtonClicked = PublishSubject.create();
    private final PublishSubject<String> passwordEditTextChanged = PublishSubject.create();
    private final PublishSubject<Boolean> resetPasswordConfirmationDialogDismissed = PublishSubject.create();
    private final PublishSubject<String> toolbarTitleText = PublishSubject.create();

    private final Observable<String> genericLoginError;
    private final Observable<String> invalidloginError;
    private final BehaviorSubject<Boolean> logInButtonIsEnabled = BehaviorSubject.create();
    private final PublishSubject<Void> loginSuccess = PublishSubject.create();
    private final PublishSubject<String> preFillEmailFromPasswordReset = PublishSubject.create();
    private final BehaviorSubject<Pair<Boolean, String>> showResetPasswordSuccessDialog = BehaviorSubject.create();
    private final Observable<Void> tfaChallenge;

    private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void backOrCloseButtonClicked() {
      this.backOrCloseButtonClicked.onNext(null);
    }
    @Override public void emailEditTextChanged(String email) {
      this.emailEditTextChanged.onNext(email);
    }
    @Override public void logInButtonClicked() {
      logInButtonClicked.onNext(null);
    }
    @Override public void passwordEditTextChanged(String password) {
      this.passwordEditTextChanged.onNext(password);
    }
    @Override public void resetPasswordConfirmationDialogDismissed() {
      this.resetPasswordConfirmationDialogDismissed.onNext(true);
    }
    @Override public void toolbarTitleText(String title) {
      this.toolbarTitleText.onNext(title);
    }

    @Override
    public Observable<String> genericLoginError() {
      return genericLoginError;
    }
    @Override public @NonNull Observable<String> invalidLoginError() {
      return this.invalidloginError;
    }
    @Override public Observable<Boolean> logInButtonIsEnabled() {
      return logInButtonIsEnabled;
    }
    @Override public @NonNull Observable<Void> loginSuccess() {
      return this.loginSuccess;
    }
    @Override public @NonNull Observable<String> preFillEmailFromPasswordReset() {
      return this.preFillEmailFromPasswordReset;
    }
    @Override public @NonNull Observable<Pair<Boolean, String>> showResetPasswordSuccessDialog() {
      return this.showResetPasswordSuccessDialog;
    }
    @Override public @NonNull Observable<Void> tfaChallenge() {
      return this.tfaChallenge;
    }
  }
}
