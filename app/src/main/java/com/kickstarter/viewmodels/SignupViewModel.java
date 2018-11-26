package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.SignupActivity;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface SignupViewModel {

  interface Inputs {
    /** Call when the email field changes. */
    void email(String email);

    /** Call when the name field changes. */
    void name(String name);

    /** Call when the password field changes. */
    void password(String password);

    /** Call when the send newsletter toggle changes. */
    void sendNewslettersClick(boolean send);

    /** Call when the signup button has been clicked. */
    void signupClick();
  }

  interface Outputs {
    /** Emits a string to display when signup fails. */
    Observable<String> errorString();

    /** Emits a boolean that determines if the sign up button is enabled. */
    Observable<Boolean> formIsValid();

    /** Emits a boolean that determines if the sign up button is disabled. */
    Observable<Boolean> formSubmitting();

    /** Emits a boolean that determines if the send newsletter toggle is checked. */
    Observable<Boolean> sendNewslettersIsChecked();

    /** Finish the activity with a successful result. */
    Observable<Void> signupSuccess();
  }

  final class ViewModel extends ActivityViewModel<SignupActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;
    private final CurrentConfigType currentConfig;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentConfig = environment.currentConfig();
      this.currentUser = environment.currentUser();

      final Observable<SignupData> signupData = Observable.combineLatest(
        this.name, this.email, this.password, this.sendNewslettersIsChecked, SignupData::new
      );

      this.sendNewslettersClick
        .compose(bindToLifecycle())
        .subscribe(this.sendNewslettersIsChecked::onNext);

      signupData
        .map(SignupData::isValid)
        .compose(bindToLifecycle())
        .subscribe(this.formIsValid);

      signupData
        .compose(takeWhen(this.signupClick))
        .flatMap(this::submit)
        .compose(bindToLifecycle())
        .subscribe(this::success);

      this.currentConfig.observable()
        .take(1)
        .map(config -> false)
        .compose(bindToLifecycle())
        .subscribe(this.sendNewslettersIsChecked::onNext);

      this.signupError
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackRegisterError());

      this.errorString = this.signupError
        .takeUntil(this.signupSuccess)
        .map(ErrorEnvelope::errorMessage);

      this.sendNewslettersClick
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackSignupNewsletterToggle);

      this.signupSuccess
        .compose(bindToLifecycle())
        .subscribe(__ -> {
          this.koala.trackLoginSuccess();
          this.koala.trackRegisterSuccess();
        });

      this.koala.trackRegisterFormView();
    }

    private Observable<AccessTokenEnvelope> submit(final @NonNull SignupData data) {
      return this.client.signup(data.name, data.email, data.password, data.password, data.sendNewsletters)
        .compose(Transformers.pipeApiErrorsTo(this.signupError))
        .compose(Transformers.neverError())
        .doOnSubscribe(() -> this.formSubmitting.onNext(true))
        .doAfterTerminate(() -> this.formSubmitting.onNext(false));
    }

    private void success(final @NonNull AccessTokenEnvelope envelope) {
      this.currentUser.login(envelope.user(), envelope.accessToken());
      this.signupSuccess.onNext(null);
    }

    private final PublishSubject<String> email = PublishSubject.create();
    private final PublishSubject<String> name = PublishSubject.create();
    private final PublishSubject<String> password = PublishSubject.create();
    private final PublishSubject<Boolean> sendNewslettersClick = PublishSubject.create();
    private final PublishSubject<Void> signupClick = PublishSubject.create();

    private final Observable<String> errorString;
    private final PublishSubject<Void> signupSuccess = PublishSubject.create();
    private final BehaviorSubject<Boolean> formSubmitting = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> formIsValid = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> sendNewslettersIsChecked = BehaviorSubject.create();

    private final PublishSubject<ErrorEnvelope> signupError = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void email(final String email) {
      this.email.onNext(email);
    }
    @Override public void name(final String name) {
      this.name.onNext(name);
    }
    @Override public void password(final String password) {
      this.password.onNext(password);
    }
    @Override public void sendNewslettersClick(final boolean send) {
      this.sendNewslettersClick.onNext(send);
    }
    @Override public void signupClick() {
      this.signupClick.onNext(null);
    }


    @Override public @NonNull Observable<String> errorString() {
      return this.errorString;
    }
    @Override public @NonNull BehaviorSubject<Boolean> formIsValid() {
      return this.formIsValid;
    }
    @Override public @NonNull BehaviorSubject<Boolean> formSubmitting() {
      return this.formSubmitting;
    }
    @Override public @NonNull BehaviorSubject<Boolean> sendNewslettersIsChecked() {
      return this.sendNewslettersIsChecked;
    }
    @Override public @NonNull PublishSubject<Void> signupSuccess() {
      return this.signupSuccess;
    }

    final static class SignupData {
      final @NonNull String email;
      final @NonNull String name;
      final @NonNull String password;
      final boolean sendNewsletters;

      SignupData(final @NonNull String name, final @NonNull String email, final @NonNull String password,
        final boolean sendNewsletters) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.sendNewsletters = sendNewsletters;
      }

      boolean isValid() {
        return this.name.length() > 0 && StringUtils.isEmail(this.email) && this.password.length() >= 6;
      }
    }
  }
}
