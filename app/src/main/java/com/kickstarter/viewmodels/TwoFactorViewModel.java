package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.TwoFactorActivity;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

public interface TwoFactorViewModel {

  interface Inputs {
    /** Call when the 2FA code has been submitted. */
    void code(String __);

    /** Call when the log in button has been clicked. */
    void loginClick();

    /** Call when the resend button has been clicked. */
    void resendClick();
  }

  interface Outputs {
    /** Emits when submitting TFA code errored for an unknown reason. */
    Observable<Void> genericTfaError();

    /** Emits when TFA code was submitted. */
    Observable<Boolean> formSubmitting();

    /** Emits when TFA code submission has completed. */
    Observable<Boolean> formIsValid();

    /** Emits when resend code confirmation should be shown. */
    Observable<Void> showResendCodeConfirmation();

    /** Emits when a submitted TFA code does not match. */
    Observable<Void> tfaCodeMismatchError();

    /** Emits when submitting TFA code was successful. */
    Observable<Void> tfaSuccess();
  }

  final class ViewModel extends ActivityViewModel<TwoFactorActivity> implements Inputs, Outputs{
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.currentUser = environment.currentUser();
      this.client = environment.apiClient();

      final Observable<String> email = intent()
        .map(i -> i.getStringExtra(IntentKey.EMAIL));
      final Observable<String> fbAccessToken = intent()
        .map(i -> i.getStringExtra(IntentKey.FACEBOOK_TOKEN));
      final Observable<Boolean> isFacebookLogin = intent()
        .map(i -> i.getBooleanExtra(IntentKey.FACEBOOK_LOGIN, false));
      final Observable<String> password = intent()
        .map(i -> i.getStringExtra(IntentKey.PASSWORD));

      final Observable<TfaData> tfaData = Observable.combineLatest(
        email, fbAccessToken, isFacebookLogin, password, TfaData::new
      );

      this.code
        .map(TwoFactorViewModel.ViewModel::isCodeValid)
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

      this.lake.trackTwoFactorConfirmationViewed();
    }

    private void success(final @NonNull AccessTokenEnvelope envelope) {
      this.currentUser.login(envelope.user(), envelope.accessToken());
      this.tfaSuccess.onNext(null);
    }

    private @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String code, final @NonNull String email,
      final @NonNull String password) {
      return this.client.login(email, password, code)
        .compose(Transformers.pipeApiErrorsTo(this.tfaError))
        .compose(Transformers.neverError())
        .doOnSubscribe(() -> this.formSubmitting.onNext(true))
        .doAfterTerminate(() -> this.formSubmitting.onNext(false));
    }

    private @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String code, final @NonNull String fbAccessToken) {
      return this.client.loginWithFacebook(fbAccessToken, code)
        .compose(Transformers.pipeApiErrorsTo(this.tfaError))
        .compose(Transformers.neverError())
        .doOnSubscribe(() -> this.formSubmitting.onNext(true))
        .doAfterTerminate(() -> this.formSubmitting.onNext(false));
    }

    private @NonNull Observable<AccessTokenEnvelope> resendCode(final @NonNull String email, final @NonNull String password) {
      return this.client.login(email, password)
        .compose(Transformers.neverError())
        .doOnSubscribe(() -> this.showResendCodeConfirmation.onNext(null));
    }

    private @NonNull Observable<AccessTokenEnvelope> resendCodeWithFacebook(final @NonNull String fbAccessToken) {
      return this.client.loginWithFacebook(fbAccessToken)
        .compose(Transformers.neverError())
        .doOnSubscribe(() -> this.showResendCodeConfirmation.onNext(null));
    }

    private static boolean isCodeValid(final String code) {
      return code != null && code.length() > 0;
    }

    private final PublishSubject<String> code = PublishSubject.create();
    private final PublishSubject<Void> loginClick = PublishSubject.create();
    private final PublishSubject<Void> resendClick = PublishSubject.create();

    private final PublishSubject<Boolean> formIsValid = PublishSubject.create();
    private final PublishSubject<Boolean> formSubmitting = PublishSubject.create();
    private final PublishSubject<Void> showResendCodeConfirmation = PublishSubject.create();
    private final PublishSubject<ErrorEnvelope> tfaError = PublishSubject.create();
    private final PublishSubject<Void> tfaSuccess = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public Observable<Boolean> formIsValid() {
      return this.formIsValid;
    }
    @Override public Observable<Boolean> formSubmitting() {
      return this.formSubmitting;
    }
    @Override public Observable<Void> genericTfaError() {
      return this.tfaError
        .filter(env -> !env.isTfaFailedError())
        .map(__ -> null);
    }
    @Override public Observable<Void> showResendCodeConfirmation() {
      return this.showResendCodeConfirmation;
    }
    @Override public Observable<Void> tfaCodeMismatchError() {
      return this.tfaError
        .filter(ErrorEnvelope::isTfaFailedError)
        .map(__ -> null);
    }
    @Override public Observable<Void> tfaSuccess() {
      return this.tfaSuccess;
    }

    @Override public void code(final @NonNull String s) {
      this.code.onNext(s);
    }
    @Override public void loginClick() {
      this.loginClick.onNext(null);
    }
    @Override public void resendClick() {
      this.resendClick.onNext(null);
    }

    protected final class TfaData {
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
  }
}
