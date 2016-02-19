package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.ResetPasswordActivity;
import com.kickstarter.viewmodels.errors.ResetPasswordViewModelErrors;
import com.kickstarter.viewmodels.inputs.ResetPasswordViewModelInputs;
import com.kickstarter.viewmodels.outputs.ResetPasswordViewModelOutputs;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class ResetPasswordViewModel extends ViewModel<ResetPasswordActivity> implements ResetPasswordViewModelInputs,
  ResetPasswordViewModelOutputs, ResetPasswordViewModelErrors {

  // INPUTS
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<Void> resetPasswordClick = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> resetSuccess = PublishSubject.create();
  public final Observable<Void> resetSuccess() {
    return resetSuccess.asObservable();
  }
  private final PublishSubject<Boolean> isFormSubmitting = PublishSubject.create();
  public final Observable<Boolean> isFormSubmitting() {
    return isFormSubmitting.asObservable();
  }
  private final PublishSubject<Boolean> isFormValid = PublishSubject.create();
  public final Observable<Boolean> isFormValid() {
    return isFormValid.asObservable();
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> resetError = PublishSubject.create();
  public final Observable<String> resetError() {
    return resetError
      .takeUntil(resetSuccess)
      .map(ErrorEnvelope::errorMessage);
  }

  private final ApiClientType client;

  public final ResetPasswordViewModelInputs inputs = this;
  public final ResetPasswordViewModelOutputs outputs = this;
  public final ResetPasswordViewModelErrors errors = this;

  @Override
  public void email(final @NonNull String s) {
    email.onNext(s);
  }

  @Override
  public void resetPasswordClick() {
    resetPasswordClick.onNext(null);
  }

  public ResetPasswordViewModel(final @NonNull Environment environment) {
    super(environment);

    this.client = environment.apiClient();

    email
      .map(StringUtils::isEmail)
      .compose(bindToLifecycle())
      .subscribe(isFormValid);

    email
      .compose(Transformers.takeWhen(resetPasswordClick))
      .switchMap(this::submitEmail)
      .compose(bindToLifecycle())
      .subscribe(__ -> success());
  }

  @Override
  public void onCreate(final @NonNull Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    resetError
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackResetPasswordError());

    resetSuccess
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackResetPasswordSuccess());

    koala.trackResetPasswordFormView();
  }

  private Observable<User> submitEmail(final @NonNull String email) {
    return client.resetPassword(email)
      .compose(Transformers.pipeApiErrorsTo(resetError))
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> isFormSubmitting.onNext(true))
      .finallyDo(() -> isFormSubmitting.onNext(false));
  }

  private void success() {
    resetSuccess.onNext(null);
  }
}
