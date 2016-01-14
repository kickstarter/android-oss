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
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.SignupActivity;
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
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<String> password = PublishSubject.create();
  private final PublishSubject<Boolean> sendNewsletters = PublishSubject.create();
  private final PublishSubject<Void> signupClick = PublishSubject.create();
  private final PublishSubject<Boolean> checkInitialNewsletterInput = PublishSubject.create();

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
  private final BehaviorSubject<Boolean> checkInitialNewsletter = BehaviorSubject.create();
  public final Observable<Boolean> checkInitialNewsletter() {
    return checkInitialNewsletter;
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
  public void fullName(@NonNull final String s) {
    fullName.onNext(s);
  }

  @Override
  public void email(@NonNull final String s) {
    email.onNext(s);
  }

  @Override
  public void password(@NonNull final String s) {
    password.onNext(s);
  }

  @Override
  public void sendNewsletters(final boolean b) {
    sendNewsletters.onNext(b);
  }

  @Override
  public void signupClick() {
    signupClick.onNext(null);
  }

  public SignupViewModel() {
    final Observable<SignupData> signupData = Observable.combineLatest(fullName, email, password, sendNewsletters, SignupData::new);

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
  }

  @Override
  public void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    addSubscription(checkInitialNewsletterInput.subscribe(checkInitialNewsletter));
    checkInitialNewsletterInput.onNext(this.isInitialNewsletterChecked());

    addSubscription(signupError.subscribe(__ -> koala.trackRegisterError()));

    addSubscription(sendNewsletters.subscribe(koala::trackSignupNewsletterToggle));

    addSubscription(signupSuccess
        .subscribe(__ -> {
          koala.trackLoginSuccess();
          koala.trackRegisterSuccess();
        })
    );

    koala.trackRegisterFormView();
  }

  private Observable<AccessTokenEnvelope> submit(@NonNull final SignupData data) {
    return client.signup(data.fullName, data.email, data.password, data.password, data.sendNewsletters)
      .compose(Transformers.pipeApiErrorsTo(signupError))
      .doOnSubscribe(() -> formSubmitting.onNext(true))
      .finallyDo(() -> formSubmitting.onNext(false));
  }

  private void success(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    signupSuccess.onNext(null);
  }

  private boolean isInitialNewsletterChecked() {
    return currentConfig.getConfig().countryCode().equals("US");
  }
}
