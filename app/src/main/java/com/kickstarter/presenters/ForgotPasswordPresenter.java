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
import com.kickstarter.models.User;
import com.kickstarter.presenters.errors.ForgotPasswordPresenterErrors;
import com.kickstarter.presenters.inputs.ForgotPasswordPresenterInputs;
import com.kickstarter.presenters.outputs.ForgotPasswordPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.ForgotPasswordActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public final class ForgotPasswordPresenter extends Presenter<ForgotPasswordActivity> implements ForgotPasswordPresenterInputs,
  ForgotPasswordPresenterOutputs, ForgotPasswordPresenterErrors {

  // INPUTS
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<Void> resetPasswordClick = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> resetSuccess = PublishSubject.create();
  public final Observable<Void> resetSuccess() {
    return resetSuccess.asObservable();
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
  private final PublishSubject<ErrorEnvelope> resetError = PublishSubject.create();
  public final Observable<String> resetError() {
    return resetError
      .takeUntil(resetSuccess)
      .map(ErrorEnvelope::errorMessage);
  }

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public final ForgotPasswordPresenterInputs inputs = this;
  public final ForgotPasswordPresenterOutputs outputs = this;
  public final ForgotPasswordPresenterErrors errors = this;

  @Override
  public void email(@NonNull final String s) {
    email.onNext(s);
  }

  @Override
  public void resetPasswordClick() {
    resetPasswordClick.onNext(null);
  }

  public ForgotPasswordPresenter() {

    addSubscription(email
        .map(ForgotPasswordPresenter::isValid)
        .subscribe(this.formIsValid::onNext)
    );

    addSubscription(email
      .compose(Transformers.takeWhen(resetPasswordClick))
      .switchMap(this::submitEmail)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::success));
  }

  @Override
  public void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
  }

  private static boolean isValid(@NonNull final String email) {
    return StringUtils.isEmail(email);
  }

  private Observable<User> submitEmail(@NonNull final String email) {
    return client.resetPassword(email)
      .compose(Transformers.pipeApiErrorsTo(resetError))
      .doOnSubscribe(() -> formSubmitting.onNext(true))
      .finallyDo(() -> formSubmitting.onNext(false));
  }

  private void success(@NonNull User user) {
    resetSuccess.onNext(null);
  }
}
