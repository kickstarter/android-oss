package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.presenters.errors.SignupPresenterErrors;
import com.kickstarter.presenters.inputs.SignupPresenterInputs;
import com.kickstarter.presenters.outputs.SignupPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.SignupActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class SignupPresenter extends Presenter<SignupActivity> implements SignupPresenterInputs, SignupPresenterOutputs,
SignupPresenterErrors {

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

  // ERRORS
  private final PublishSubject<ErrorEnvelope> signupError = PublishSubject.create();
  public final Observable<String> signupError() {
    return signupError
      .takeUntil(signupSuccess)
      .map(ErrorEnvelope::errorMessage);
  }

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public final SignupPresenterInputs inputs = this;
  public final SignupPresenterOutputs outputs = this;
  public final SignupPresenterErrors errors = this;

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

  public SignupPresenter() {
    final Observable<SignupData> signupData = Observable.combineLatest(fullName, email, password, sendNewsletters, SignupData::new);

    addSubscription(signupData
        .map(SignupData::isValid)
        .subscribe(this.formIsValid::onNext)
    );

    addSubscription(
      signupData
        .compose(Transformers.takeWhen(signupClick))
        .flatMap(this::submit)
        .subscribe(this::success)
    );

    addSubscription(signupSuccess
        .subscribe(__ -> {
          koala.trackLoginSuccess();
          koala.trackRegisterSuccess();
        })
    );

    addSubscription(signupError
        .subscribe(__ -> koala.trackRegisterError())
    );

    addSubscription(sendNewsletters
      .subscribe(koala::trackSignupNewsletterToggle)
    );

    koala.trackRegisterFormView();
  }

  @Override
  public void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
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
}
