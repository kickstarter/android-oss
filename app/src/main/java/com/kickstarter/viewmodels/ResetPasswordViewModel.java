package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
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

public final class ResetPasswordViewModel extends ActivityViewModel<ResetPasswordActivity> implements ResetPasswordViewModelInputs,
  ResetPasswordViewModelOutputs, ResetPasswordViewModelErrors {

  // INPUTS
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<Void> resetPasswordClick = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> resetSuccess = PublishSubject.create();
  public Observable<Void> resetSuccess() {
    return this.resetSuccess.asObservable();
  }
  private final PublishSubject<Boolean> isFormSubmitting = PublishSubject.create();
  public Observable<Boolean> isFormSubmitting() {
    return this.isFormSubmitting.asObservable();
  }
  private final PublishSubject<Boolean> isFormValid = PublishSubject.create();
  public Observable<Boolean> isFormValid() {
    return this.isFormValid.asObservable();
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> resetError = PublishSubject.create();
  public Observable<String> resetError() {
    return this.resetError
      .takeUntil(this.resetSuccess)
      .map(ErrorEnvelope::errorMessage);
  }

  private final ApiClientType client;

  public final ResetPasswordViewModelInputs inputs = this;
  public final ResetPasswordViewModelOutputs outputs = this;
  public final ResetPasswordViewModelErrors errors = this;

  @Override
  public void email(final @NonNull String s) {
    this.email.onNext(s);
  }

  @Override
  public void resetPasswordClick() {
    this.resetPasswordClick.onNext(null);
  }

  public ResetPasswordViewModel(final @NonNull Environment environment) {
    super(environment);

    this.client = environment.apiClient();

    this.email
      .map(StringUtils::isEmail)
      .compose(bindToLifecycle())
      .subscribe(this.isFormValid);

    this.email
      .compose(Transformers.takeWhen(this.resetPasswordClick))
      .switchMap(this::submitEmail)
      .compose(bindToLifecycle())
      .subscribe(__ -> success());

    this.resetError
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackResetPasswordError());

    this.resetSuccess
      .compose(bindToLifecycle())
      .subscribe(__ -> this.koala.trackResetPasswordSuccess());

    this.koala.trackResetPasswordFormView();
  }

  private Observable<User> submitEmail(final @NonNull String email) {
    return this.client.resetPassword(email)
      .compose(Transformers.pipeApiErrorsTo(this.resetError))
      .compose(Transformers.neverError())
      .doOnSubscribe(() -> this.isFormSubmitting.onNext(true))
      .doAfterTerminate(() -> this.isFormSubmitting.onNext(false));
  }

  private void success() {
    this.resetSuccess.onNext(null);
  }
}
