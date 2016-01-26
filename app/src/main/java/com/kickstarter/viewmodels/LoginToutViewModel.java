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

  private final BehaviorSubject<LoginReason> startLogin = BehaviorSubject.create();
  @Override
  public @NonNull Observable<LoginReason> startLogin() {
    return startLogin;
  }

  private final BehaviorSubject<LoginReason> startSignup = BehaviorSubject.create();
  @Override
  public @NonNull Observable<LoginReason> startSignup() {
    return startSignup;
  }

  // ERRORS
  @Override
  public @NonNull Observable<Pair<ErrorEnvelope.FacebookUser, LoginReason>> confirmFacebookSignupError() {
    return loginError
      .filter(ErrorEnvelope::isConfirmFacebookSignupError)
      .map(ErrorEnvelope::facebookUser)
      .compose(Transformers.combineLatestPair(loginReason));
  }

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
  public Observable<LoginReason> startTwoFactorChallenge() {
    return loginReason
      .compose(Transformers.takeWhen(loginError.filter(ErrorEnvelope::isTfaRequiredError)));
  }

  protected @Inject CurrentUser currentUser;
  protected @Inject ApiClientType client;

  private Observable<LoginReason> loginReason;
  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();

  public final LoginToutViewModelInputs inputs = this;
  public final LoginToutViewModelOutputs outputs = this;
  public final LoginToutViewModelErrors errors = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    registerFacebookCallback();

    loginReason = intent
      .map(i -> i.getSerializableExtra(IntentKey.LOGIN_REASON))
      .ofType(LoginReason.class);

    Observable<AccessTokenEnvelope> facebookLoginSuccess = facebookAccessToken
      .switchMap(this::loginWithFacebookAccessToken)
      .share();

    addSubscription(loginReason.take(1).subscribe(koala::trackLoginRegisterTout));

    addSubscription(loginError.subscribe(__ -> koala.trackLoginError()));

    addSubscription(activityResult
        .subscribe(r -> callbackManager.onActivityResult(r.requestCode(), r.resultCode(), r.intent()))
    );

    addSubscription(facebookAuthorizationError
        .subscribe(this::clearFacebookSession)
    );

    addSubscription(facebookLoginSuccess.subscribe(envelope -> {
      currentUser.login(envelope.user(), envelope.accessToken());
      finishWithSuccessfulResult.onNext(null);
    }));

    addSubscription(facebookLoginSuccess.subscribe(__ -> koala.trackFacebookLoginSuccess()));

    addSubscription(loginReason
      .compose(Transformers.takeWhen(loginClick))
      .subscribe(startLogin::onNext));

    addSubscription(loginReason
      .compose(Transformers.takeWhen(signupClick))
      .subscribe(startSignup::onNext));

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
