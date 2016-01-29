package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
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

  // INPUTS
  @Override
  public void facebookLoginClick(@NonNull final LoginToutActivity activity, @NonNull List<String> facebookPermissions) {
    LoginManager.getInstance().logInWithReadPermissions(activity, facebookPermissions);
  }

  private final PublishSubject<Void> loginClick = PublishSubject.create();
  @Override
  public void loginClick() {
    loginClick.onNext(null);
  }

  private final PublishSubject<Void> signupClick = PublishSubject.create();
  @Override
  public void signupClick() {
    signupClick.onNext(null);
  }

  // OUTPUTS
  private final BehaviorSubject<Void> finishWithSuccessfulResult = BehaviorSubject.create();
  @Override
  public @NonNull Observable<Void> finishWithSuccessfulResult() {
    return finishWithSuccessfulResult;
  }

  private final BehaviorSubject<Void> startLogin = BehaviorSubject.create();
  @Override
  public @NonNull Observable<Void> startLogin() {
    return startLogin;
  }

  private final BehaviorSubject<Void> startSignup = BehaviorSubject.create();
  @Override
  public @NonNull Observable<Void> startSignup() {
    return startSignup;
  }

  private final BehaviorSubject<Pair<ErrorEnvelope.FacebookUser, String>> startConfirmFacebookSignup = BehaviorSubject.create();
  @Override
  public @NonNull Observable<Pair<ErrorEnvelope.FacebookUser, String>> startConfirmFacebookSignup() {
    return startConfirmFacebookSignup;
  }

  // ERRORS
  private final PublishSubject<FacebookException> facebookAuthorizationError = PublishSubject.create();
  @Override
  public @NonNull Observable<String> facebookAuthorizationError() {
    return facebookAuthorizationError
      .map(FacebookException::getLocalizedMessage);
  }

  @Override
  public @NonNull Observable<String> facebookInvalidAccessTokenError() {
    return loginError
      .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
      .map(ErrorEnvelope::errorMessage);
  }

  @Override
  public @NonNull Observable<String> missingFacebookEmailError() {
    return loginError
      .filter(ErrorEnvelope::isMissingFacebookEmailError)
      .map(ErrorEnvelope::errorMessage);
  }

  @Override
  public Observable<Void> startTwoFactorChallenge() {
    return loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }

  private CallbackManager callbackManager;
  private final PublishSubject<String> facebookAccessToken = PublishSubject.create();
  private final PublishSubject<LoginReason> loginReason = PublishSubject.create();
  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();

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

    Observable<AccessTokenEnvelope> facebookSuccessTokenEnvelope = facebookAccessToken
      .switchMap(this::loginWithFacebookAccessToken)
      .share();

    intent
      .map(i -> i.getSerializableExtra(IntentKey.LOGIN_REASON))
      .ofType(LoginReason.class)
      .compose(bindToLifecycle())
      .subscribe(loginReason::onNext);

    activityResult
      .compose(bindToLifecycle())
      .subscribe(r -> callbackManager.onActivityResult(r.requestCode(), r.resultCode(), r.intent()));

    activityResult
      .filter(r -> r.isRequestCode(ActivityRequestCodes.LOGIN_FLOW))
      .filter(ActivityResult::isOk)
      .compose(bindToLifecycle())
      .subscribe(__ -> finishWithSuccessfulResult.onNext(null));

    facebookAuthorizationError
      .compose(bindToLifecycle())
      .subscribe(this::clearFacebookSession);

    facebookSuccessTokenEnvelope
      .compose(bindToLifecycle())
      .subscribe(envelope -> {
        currentUser.login(envelope.user(), envelope.accessToken());
        finishWithSuccessfulResult.onNext(null);
      });

    loginClick
      .compose(bindToLifecycle())
      .subscribe(startLogin::onNext);

    signupClick
      .compose(bindToLifecycle())
      .subscribe(startSignup::onNext);

    loginError
      .filter(ErrorEnvelope::isConfirmFacebookSignupError)
      .map(ErrorEnvelope::facebookUser)
      .compose(Transformers.combineLatestPair(facebookAccessToken))
      .compose(bindToLifecycle())
      .subscribe(startConfirmFacebookSignup::onNext);

    loginReason.take(1)
      .compose(bindToLifecycle())
      .subscribe(koala::trackLoginRegisterTout);

    loginError
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackLoginError());

    loginError
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackLoginError());

    missingFacebookEmailError()
      .mergeWith(facebookInvalidAccessTokenError())
      .mergeWith(facebookAuthorizationError())
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackFacebookLoginError());
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
      public void onSuccess(final @NonNull LoginResult result) {
        facebookAccessToken.onNext(result.getAccessToken().getToken());
      }

      @Override
      public void onCancel() {
        // continue
      }

      @Override
      public void onError(final @NonNull FacebookException error) {
        if (error instanceof FacebookAuthorizationException) {
          facebookAuthorizationError.onNext(error);
        }
      }
    });
  }
}
