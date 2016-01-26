package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentConfig;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.I18nUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.SignupActivity;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.viewmodels.errors.SignupViewModelErrors;
import com.kickstarter.viewmodels.inputs.SignupViewModelInputs;
import com.kickstarter.viewmodels.outputs.SignupViewModelOutputs;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class SignupViewModel extends ViewModel<SignupActivity> implements SignupViewModelInputs, SignupViewModelOutputs,
  SignupViewModelErrors {
  protected @Inject ApiClientType client;
  protected @Inject CurrentUser currentUser;
  protected @Inject CurrentConfig currentConfig;

  protected final static class SignupData {
    @NonNull final String fullName;
    @NonNull final String email;
    @NonNull final String password;
    final boolean sendNewsletters;

    protected SignupData(@NonNull final String fullName, @NonNull final String email, @NonNull final String password,
      final boolean sendNewsletters) {
      this.fullName = fullName;
      this.email = email;
      this.password = password;
      this.sendNewsletters = sendNewsletters;
    }

    protected boolean isValid() {
      return fullName.length() > 0 && StringUtils.isEmail(email) && password.length() >= 6;
    }
  }

  // INPUTS
  private final PublishSubject<String> fullName = PublishSubject.create();
  public void fullName(final @NonNull String s) {
    fullName.onNext(s);
  }
  private final PublishSubject<String> email = PublishSubject.create();
  public void email(final @NonNull String s) {
    email.onNext(s);
  }
  private final PublishSubject<String> password = PublishSubject.create();
  public void password(final @NonNull String s) {
    password.onNext(s);
  }
  private final PublishSubject<Boolean> sendNewslettersClick = PublishSubject.create();
  public void sendNewslettersClick(final boolean b) {
    sendNewslettersClick.onNext(b);
  }
  private final PublishSubject<Void> signupClick = PublishSubject.create();
  public void signupClick() {
    signupClick.onNext(null);
  }

  // OUTPUTS
  private final PublishSubject<Void> signupSuccess = PublishSubject.create();
  public final Observable<Void> signupSuccess() {
    return signupSuccess.asObservable();
  }
  private final PublishSubject<Boolean> formSubmitting = PublishSubject.create();
  public final Observable<Boolean> formSubmitting() {
    return formSubmitting.asObservable();
  }
  private final PublishSubject<Boolean> formIsValid = PublishSubject.create();
  public final Observable<Boolean> formIsValid() {
    return formIsValid.asObservable();
  }
  final BehaviorSubject<Boolean> sendNewslettersIsChecked = BehaviorSubject.create();
  public final Observable<Boolean> sendNewslettersIsChecked() {
    return sendNewslettersIsChecked;
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> signupError = PublishSubject.create();
  public final Observable<String> signupError() {
    return signupError
      .takeUntil(signupSuccess)
      .map(ErrorEnvelope::errorMessage);
  }

  public final SignupViewModelInputs inputs = this;
  public final SignupViewModelOutputs outputs = this;
  public final SignupViewModelErrors errors = this;

  @Override
  public void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<SignupData> signupData = Observable.combineLatest(
      fullName, email, password, sendNewslettersIsChecked,
      SignupData::new);

    addSubscription(
      sendNewslettersClick.subscribe(sendNewslettersIsChecked::onNext)
    );

    addSubscription(signupData
        .map(SignupData::isValid)
        .subscribe(formIsValid)
    );

    addSubscription(
      signupData
        .compose(Transformers.takeWhen(signupClick))
        .flatMap(this::submit)
        .subscribe(this::success)
    );

    currentConfig.observable()
      .take(1)
      .map(config -> I18nUtils.isCountryUS(config.countryCode()))
      .subscribe(sendNewslettersIsChecked::onNext);

    addSubscription(signupError.subscribe(__ -> koala.trackRegisterError()));
    addSubscription(sendNewslettersClick.subscribe(koala::trackSignupNewsletterToggle));
    addSubscription(signupSuccess
        .subscribe(__ -> {
          koala.trackLoginSuccess();
          koala.trackRegisterSuccess();
        })
    );
    koala.trackRegisterFormView();
  }

  private Observable<AccessTokenEnvelope> submit(final @NonNull SignupData data) {
    return client.signup(data.fullName, data.email, data.password, data.password, data.sendNewsletters)
      .compose(Transformers.pipeApiErrorsTo(signupError))
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> formSubmitting.onNext(true))
      .finallyDo(() -> formSubmitting.onNext(false));
  }

  private void success(final @NonNull AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    signupSuccess.onNext(null);
  }
}
