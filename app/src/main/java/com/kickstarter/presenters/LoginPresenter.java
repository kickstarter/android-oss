package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.utils.RxUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.presenters.errors.LoginPresenterErrors;
import com.kickstarter.presenters.inputs.LoginPresenterInputs;
import com.kickstarter.presenters.outputs.LoginPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.ApiError;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.LoginActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class LoginPresenter extends Presenter<LoginActivity> implements LoginPresenterInputs, LoginPresenterOutputs , LoginPresenterErrors {
  // INPUTS
  private final PublishSubject<String> email = PublishSubject.create();
  private final PublishSubject<View> loginClick = PublishSubject.create();
  private final PublishSubject<String> password = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> loginSuccessSubject = PublishSubject.create();
  public final Observable<Void> loginSuccess() { return loginSuccessSubject.asObservable(); };

  // ERRORS
  private final PublishSubject<Void> invalidLoginErrorSubject = PublishSubject.create();
  public final Observable<Void> invalidLoginError() { return invalidLoginErrorSubject.asObservable(); }
  private final PublishSubject<Void> genericLoginErrorSubject = PublishSubject.create();
  public final Observable<Void> genericLoginError() { return genericLoginErrorSubject.asObservable(); }
  private final PublishSubject<Void> tfaChallengeSubject = PublishSubject.create();
  public final Observable<Void> tfaChallenge() { return tfaChallengeSubject.asObservable(); }

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  public LoginPresenterInputs inputs() {
    return this;
  }
  public LoginPresenterOutputs outputs() {
    return this;
  }
  public LoginPresenterErrors errors() {
    return this;
  }

  @Override
  public void email(@NonNull final String s) {
    email.onNext(s);
  }

  @Override
  public void loginClick(@NonNull final View view) {
    loginClick.onNext(view);
  }

  @Override
  public void password(@NonNull final String s) {
    password.onNext(s);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<Pair<String, String>> emailAndPassword =
      RxUtils.combineLatestPair(email, password);

    final Observable<Boolean> isValid = emailAndPassword
      .map(ep -> LoginPresenter.isValid(ep.first, ep.second));

    addSubscription(RxUtils.combineLatestPair(viewSubject, isValid)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(viewAndValid -> viewAndValid.first.setFormEnabled(viewAndValid.second))
    );

    addSubscription(RxUtils.takeWhen(emailAndPassword, loginClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ep -> submit(ep.first, ep.second))
    );
  }

  private static boolean isValid(@NonNull final String email, @NonNull final String password) {
    return StringUtils.isEmail(email) && password.length() > 0;
  }

  private void submit(@NonNull final String email, @NonNull final String password) {
    client.login(email, password)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::success, this::error);
  }

  private void success(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    loginSuccessSubject.onNext(null);
  }

  private void error(@NonNull final Throwable e) {
    if (e instanceof ApiError) {
      final ApiError error = (ApiError) e;
      final ErrorEnvelope envelope = error.errorEnvelope();

      switch (envelope.ksrCode()) {
        case ErrorEnvelope.TFA_REQUIRED:
        case ErrorEnvelope.TFA_FAILED:
          tfaChallengeSubject.onNext(null);
          break;
        case ErrorEnvelope.INVALID_XAUTH_LOGIN:
          invalidLoginErrorSubject.onNext(null);
          break;
        default:
          genericLoginErrorSubject.onNext(null);
          break;
      }
    }
  }
}
