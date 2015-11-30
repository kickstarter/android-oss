package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.viewmodels.errors.LoginViewModelErrors;
import com.kickstarter.viewmodels.inputs.LoginViewModelInputs;
import com.kickstarter.viewmodels.outputs.LoginViewModelOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.LoginActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public final class LoginViewModel extends ViewModel<LoginActivity> implements LoginViewModelInputs, LoginViewModelOutputs, LoginViewModelErrors {
  // INPUTS
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<View> loginClick = PublishSubject.create();
  private final PublishSubject<String> password = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  public final Observable<Void> loginSuccess() {
    return loginSuccess.asObservable();
  }

  // ERRORS
  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
  public final Observable<String> invalidLoginError() {
    return loginError
      .filter(ErrorEnvelope::isInvalidLoginError)
      .map(ErrorEnvelope::errorMessage);
  }
  public final Observable<Void> tfaChallenge() {
    return loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }

  public final Observable<String> genericLoginError() {
    return loginError
      .filter(ErrorEnvelope::isGenericLoginError)
      .map(ErrorEnvelope::errorMessage);
  }

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public final LoginViewModelInputs inputs = this;
  public final LoginViewModelOutputs outputs = this;
  public final LoginViewModelErrors errors = this;

  @Override
  public void email(@NonNull final String s) {
    email.onNext(s);
  }

  @Override
  public void loginClick() {
    loginClick.onNext(null);
  }

  @Override
  public void password(@NonNull final String s) {
    password.onNext(s);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<Pair<String, String>> emailAndPassword = email
      .compose(Transformers.combineLatestPair(password));

    final Observable<Boolean> isValid = emailAndPassword
      .map(ep -> LoginViewModel.isValid(ep.first, ep.second));

    addSubscription(view
        .compose(Transformers.combineLatestPair(isValid))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(viewAndValid -> viewAndValid.first.setFormEnabled(viewAndValid.second))
    );

    addSubscription(emailAndPassword
        .compose(Transformers.takeWhen(loginClick))
        .switchMap(ep -> submit(ep.first, ep.second))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::success)
    );

    addSubscription(loginSuccess.subscribe(__ -> koala.trackLoginSuccess()));

    addSubscription(invalidLoginError().mergeWith(genericLoginError())
        .subscribe(__ -> {
          koala.trackLoginError();
        })
    );
  }

  private static boolean isValid(@NonNull final String email, @NonNull final String password) {
    return StringUtils.isEmail(email) && password.length() > 0;
  }

  private Observable<AccessTokenEnvelope> submit(@NonNull final String email, @NonNull final String password) {
    return client.login(email, password)
      .compose(Transformers.pipeApiErrorsTo(loginError));
  }

  private void success(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    loginSuccess.onNext(null);
  }
}
