package com.kickstarter.viewmodels;

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
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.data.ActivityResult;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.viewmodels.errors.LoginToutViewModelErrors;
import com.kickstarter.viewmodels.inputs.LoginToutViewModelInputs;
import com.kickstarter.viewmodels.outputs.LoginToutViewModelOutputs;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class LoginToutViewModel extends ViewModel<LoginToutActivity> implements LoginToutViewModelInputs,
  LoginToutViewModelOutputs, LoginToutViewModelErrors {

  private final PublishSubject<String> facebookAccessToken = PublishSubject.create();
  private CallbackManager callbackManager;

  // INPUTS
  private final PublishSubject<ActivityResult> activityResult = PublishSubject.create();
  @Override
  public void activityResult(final @NonNull ActivityResult activityResult) {
    this.activityResult.onNext(activityResult);
  }

  @Override
  public void facebookLoginClick(@NonNull final LoginToutActivity activity, @NonNull List<String> facebookPermissions) {
    LoginManager.getInstance().logInWithReadPermissions(activity, facebookPermissions);
  }

  // OUTPUTS
  BehaviorSubject<LoginReason> loginReason = BehaviorSubject.create();
  public final @NonNull Observable<LoginReason> loginReason() {
    return loginReason;
  }

  BehaviorSubject<Void> returnResultAfterLoginSuccess = BehaviorSubject.create();
  public final @NonNull Observable<Void> returnResultAfterLoginSuccess() {
    return returnResultAfterLoginSuccess;
  }

  BehaviorSubject<Void> startDiscoveryAfterLoginSuccess = BehaviorSubject.create();
  public final @NonNull Observable<Void> startDiscoveryAfterLoginSuccess() {
    return startDiscoveryAfterLoginSuccess;
  }

  // ERRORS
  private final PublishSubject<FacebookException> facebookAuthorizationError = PublishSubject.create();
  public final Observable<String> facebookAuthorizationError() {
    return facebookAuthorizationError
      .map(FacebookException::getLocalizedMessage);
  }

  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
  public final Observable<ErrorEnvelope.FacebookUser> confirmFacebookSignupError() {
   return loginError
     .filter(ErrorEnvelope::isConfirmFacebookSignupError)
     .map(ErrorEnvelope::facebookUser);
  }

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

  protected @Inject CurrentUser currentUser;
  protected @Inject ApiClientType client;

  public final LoginToutViewModelInputs inputs = this;
  public final LoginToutViewModelOutputs outputs = this;
  public final LoginToutViewModelErrors errors = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    registerFacebookCallback();

    Observable<AccessTokenEnvelope> facebookLoginSuccess = facebookAccessToken
      .switchMap(this::loginWithFacebookAccessToken)
      .share();

    addSubscription(intent
      .map(i -> i.getSerializableExtra(IntentKey.LOGIN_REASON))
      .filter(ObjectUtils::isNotNull)
      .cast(LoginReason.class)
      .filter(ObjectUtils::isNotNull)
      .subscribe(loginReason::onNext));

    addSubscription(loginReason.take(1).subscribe(koala::trackLoginRegisterTout));

    addSubscription(loginError.subscribe(__ -> koala.trackLoginError()));

    addSubscription(activityResult
        .subscribe(r -> callbackManager.onActivityResult(r.requestCode(), r.resultCode(), r.intent()))
    );

    addSubscription(facebookAuthorizationError
        .subscribe(this::clearFacebookSession)
    );

    addSubscription(facebookLoginSuccess.subscribe(envelope -> currentUser.login(envelope.user(), envelope.accessToken())));

    addSubscription(facebookLoginSuccess.subscribe(__ -> koala.trackFacebookLoginSuccess()));

    addSubscription(loginReason
      .filter(LoginReason::doesNotReturnResult)
      .compose(Transformers.takeWhen(facebookLoginSuccess))
      .subscribe(__ -> startDiscoveryAfterLoginSuccess.onNext(null)));

    addSubscription(loginReason
      .filter(LoginReason::returnsResult)
      .compose(Transformers.takeWhen(facebookLoginSuccess))
      .subscribe(__ -> returnResultAfterLoginSuccess.onNext(null)));

    addSubscription(missingFacebookEmailError()
      .mergeWith(facebookInvalidAccessTokenError())
      .mergeWith(facebookAuthorizationError())
      .subscribe(__ -> koala.trackFacebookLoginError()));
  }

  private void clearFacebookSession(final @NonNull FacebookException e) {
    LoginManager.getInstance().logOut();
  }

  private @NonNull Observable<AccessTokenEnvelope> loginWithFacebookAccessToken(final @NonNull String fbAccessToken) {
    return client.loginWithFacebook(fbAccessToken)
      .compose(Transformers.pipeApiErrorsTo(loginError))
      .compose(Transformers.neverError());
  }

  private void registerFacebookCallback() {
    callbackManager = CallbackManager.Factory.create();
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
          facebookAuthorizationError.onNext(error);
        }
      }
    });
  }
}
