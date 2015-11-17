package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.presenters.errors.LoginToutPresenterErrors;
import com.kickstarter.presenters.inputs.LoginToutPresenterInputs;
import com.kickstarter.presenters.outputs.LoginToutPresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.LoginToutActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class LoginToutPresenter extends Presenter<LoginToutActivity> implements LoginToutPresenterInputs,
  LoginToutPresenterOutputs, LoginToutPresenterErrors {

  protected final class ActivityResultData {
    final int requestCode;
    final int resultCode;
    @NonNull final Intent intent;

    protected ActivityResultData(final int requestCode, final int resultCode, @NonNull final Intent intent) {
      this.requestCode = requestCode;
      this.resultCode = resultCode;
      this.intent = intent;
    }
  }

  // INPUTS
  private final PublishSubject<ActivityResultData> activityResult = PublishSubject.create();
  private final PublishSubject<String> facebookAccessToken = PublishSubject.create();
  private final PublishSubject<CallbackManager> facebookCallbackManager = PublishSubject.create();
  private final PublishSubject<String> reason = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> facebookLoginSuccess = PublishSubject.create();
  public final Observable<Void> facebookLoginSuccess() {
    return facebookLoginSuccess.asObservable();
  }

  // ERRORS
  private final PublishSubject<FacebookException> facebookAuthorizationException = PublishSubject.create();
  public final Observable<String> facebookAuthorizationException() {
    return facebookAuthorizationException
      .map(FacebookException::getLocalizedMessage);
  }

  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
  public final Observable<String> missingFacebookEmailError() {
    return loginError
      .filter(ErrorEnvelope::isMissingFacebookEmailError)
      .map(ErrorEnvelope::errorMessage);
  }

  public final Observable<String> facebookInvalidAccessTokenError() {
    return loginError
      .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
      .map(ErrorEnvelope::errorMessage);
  }

  public final Observable<Void> tfaChallenge() {
    return loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }

  @Inject CurrentUser currentUser;
  @Inject ApiClient client;

  public final LoginToutPresenterInputs inputs = this;
  public final LoginToutPresenterOutputs outputs = this;
  public final LoginToutPresenterErrors errors = this;

  public LoginToutPresenter() {

    addSubscription(facebookCallbackManager
      .subscribe(this::registerFacebookCallback));

    addSubscription(facebookCallbackManager
      .compose(Transformers.combineLatestPair(activityResult))
      .subscribe(cr -> cr.first.onActivityResult(cr.second.requestCode, cr.second.resultCode, cr.second.intent)
    ));

    addSubscription(facebookAccessToken
      .switchMap(this::loginWithFacebookAccessToken)
      .subscribe(this::facebookLoginSuccess));

    addSubscription(facebookAuthorizationException
      .subscribe(this::clearFacebookSession));
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
    addSubscription(reason.take(1).subscribe(koala::trackLoginRegisterTout));
  }

  @Override
  public void activityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    final ActivityResultData activityResultData = new ActivityResultData(requestCode, resultCode, intent);
    activityResult.onNext(activityResultData);
  }

  public void clearFacebookSession(@NonNull final FacebookException e) {
    LoginManager.getInstance().logOut();
  }

  @Override
  public void facebookLoginClick(@NonNull final LoginToutActivity activity, @NonNull List<String> facebookPermissions) {
    LoginManager.getInstance().logInWithReadPermissions(activity, facebookPermissions);
  }

  @Override
  public void facebookCallbackManager(@NonNull final CallbackManager callbackManager) {
    facebookCallbackManager.onNext(callbackManager);
  }

  public void facebookLoginSuccess(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    facebookLoginSuccess.onNext(null);
  }

  private Observable<AccessTokenEnvelope> loginWithFacebookAccessToken(@NonNull final String fbAccessToken) {
    return client.loginWithFacebook(fbAccessToken)
      .compose(Transformers.pipeApiErrorsTo(loginError));
  }

  public void registerFacebookCallback(@NonNull final CallbackManager callbackManager) {
    LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
      @Override
      public void onSuccess(@NonNull final LoginResult result) {
        facebookAccessToken.onNext(result.getAccessToken().getToken());
      }

      @Override
      public void onCancel() {
        // continue
      }

      @Override
      public void onError(@NonNull final FacebookException error) {
        if (error instanceof FacebookAuthorizationException) {
          facebookAuthorizationException.onNext(error);
        }
      }
    });
  }

  @Override
  public void reason(@Nullable final String r) {
    reason.onNext(r);
  }
}
