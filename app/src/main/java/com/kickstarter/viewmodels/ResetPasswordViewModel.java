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

import rx.Observable;
import rx.subjects.PublishSubject;

public interface ResetPasswordViewModel {

  interface Inputs {
    void email(String __);
    void resetPasswordClick();
  }

  interface Outputs {
    Observable<Boolean> isFormSubmitting();
    Observable<Boolean> isFormValid();
    Observable<Void> resetSuccess();
  }

  interface Errors {
    Observable<String> resetError();
  }

  final class ViewModel extends ActivityViewModel<ResetPasswordActivity> implements Inputs, Outputs, Errors {

    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
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

    private void success() {
      this.resetSuccess.onNext(null);
    }

    private Observable<User> submitEmail(final @NonNull String email) {
      return this.client.resetPassword(email)
              .compose(Transformers.pipeApiErrorsTo(this.resetError))
              .compose(Transformers.neverError())
              .doOnSubscribe(() -> this.isFormSubmitting.onNext(true))
              .doAfterTerminate(() -> this.isFormSubmitting.onNext(false));
    }

    // INPUTS
    private final PublishSubject<String> email = PublishSubject.create();
    private final PublishSubject<Void> resetPasswordClick = PublishSubject.create();

    // OUTPUTS
    private final PublishSubject<Boolean> isFormSubmitting = PublishSubject.create();
    private final PublishSubject<Boolean> isFormValid = PublishSubject.create();
    private final PublishSubject<Void> resetSuccess = PublishSubject.create();

    // ERRORS
    private final PublishSubject<ErrorEnvelope> resetError = PublishSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;
    public final Errors errors = this;

    @Override
    public void email(final @NonNull String s) {
      this.email.onNext(s);
    }

    @Override
    public void resetPasswordClick() {
      this.resetPasswordClick.onNext(null);
    }

    @Override
    public Observable<Boolean> isFormSubmitting() {
      return this.isFormSubmitting.asObservable();
    }

    @Override
    public Observable<Boolean> isFormValid() {
      return this.isFormValid.asObservable();
    }

    @Override
    public Observable<Void> resetSuccess() {
      return this.resetSuccess.asObservable();
    }

    @Override
    public Observable<String> resetError() {
      return this.resetError
              .takeUntil(this.resetSuccess)
              .map(ErrorEnvelope::errorMessage);
    }
  }
}
