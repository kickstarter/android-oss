package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.I18nUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.SignupActivity;
import com.kickstarter.viewmodels.errors.SignupViewModelErrors;
import com.kickstarter.viewmodels.inputs.SignupViewModelInputs;
import com.kickstarter.viewmodels.outputs.SignupViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public final class SignupViewModel extends ActivityViewModel<SignupActivity> implements SignupViewModelInputs, SignupViewModelOutputs,
  SignupViewModelErrors {
  private final ApiClientType client;
  private final CurrentUserType currentUser;
  private final CurrentConfigType currentConfig;

  protected final static class SignupData {
    final @NonNull String fullName;
    final @NonNull String email;
    final @NonNull String password;
    final boolean sendNewsletters;

    protected SignupData(final @NonNull String fullName, final @NonNull String email, final @NonNull String password,
      final boolean sendNewsletters) {
      this.fullName = fullName;
      this.email = email;
      this.password = password;
      this.sendNewsletters = sendNewsletters;
    }

    protected boolean isValid() {
      return this.fullName.length() > 0 && StringUtils.isEmail(this.email) && this.password.length() >= 6;
    }
  }

  // INPUTS
  private final PublishSubject<String> fullName = PublishSubject.create();
  public void fullName(final @NonNull String s) {
    this.fullName.onNext(s);
  }
  private final PublishSubject<String> email = PublishSubject.create();
  public void email(final @NonNull String s) {
    this.email.onNext(s);
  }
  private final PublishSubject<String> password = PublishSubject.create();
  public void password(final @NonNull String s) {
    this.password.onNext(s);
  }
  private final PublishSubject<Boolean> sendNewslettersClick = PublishSubject.create();
  public void sendNewslettersClick(final boolean b) {
    this.sendNewslettersClick.onNext(b);
  }
  private final PublishSubject<Void> signupClick = PublishSubject.create();
  public void signupClick() {
    this.signupClick.onNext(null);
  }

  // OUTPUTS
  private final PublishSubject<Void> signupSuccess = PublishSubject.create();
  public Observable<Void> signupSuccess() {
    return this.signupSuccess.asObservable();
  }
  private final PublishSubject<Boolean> formSubmitting = PublishSubject.create();
  public Observable<Boolean> formSubmitting() {
    return this.formSubmitting.asObservable();
  }
  private final PublishSubject<Boolean> formIsValid = PublishSubject.create();
  public Observable<Boolean> formIsValid() {
    return this.formIsValid.asObservable();
  }
  private final BehaviorSubject<Boolean> sendNewslettersIsChecked = BehaviorSubject.create();
  public Observable<Boolean> sendNewslettersIsChecked() {
    return this.sendNewslettersIsChecked;
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> signupError = PublishSubject.create();
  public Observable<String> signupError() {
    return this.signupError
      .takeUntil(this.signupSuccess)
      .map(ErrorEnvelope::errorMessage);
  }

  public final SignupViewModelInputs inputs = this;
  public final SignupViewModelOutputs outputs = this;
  public final SignupViewModelErrors errors = this;

  public SignupViewModel(final @NonNull Environment environment) {
    super(environment);

    this.client = environment.apiClient();
    this.currentConfig = environment.currentConfig();
    this.currentUser = environment.currentUser();

    final Observable<SignupData> signupData = Observable.combineLatest(
      this.fullName, this.email, this.password, this.sendNewslettersIsChecked, SignupData::new
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
      .map(config -> I18nUtils.isCountryUS(config.countryCode()))
      .compose(bindToLifecycle())
      .subscribe(this.sendNewslettersIsChecked::onNext);

    this.signupError
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackRegisterError());

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
    return this.client.signup(data.fullName, data.email, data.password, data.password, data.sendNewsletters)
      .compose(Transformers.pipeApiErrorsTo(signupError))
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> this.formSubmitting.onNext(true))
      .doAfterTerminate(() -> this.formSubmitting.onNext(false));
  }

  private void success(final @NonNull AccessTokenEnvelope envelope) {
    this.currentUser.login(envelope.user(), envelope.accessToken());
    this.signupSuccess.onNext(null);
  }
}
