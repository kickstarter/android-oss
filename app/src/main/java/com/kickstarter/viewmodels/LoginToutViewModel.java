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
  public void loginClick() {
    loginClick.onNext(null);
  }

  private final PublishSubject<Void> signupClick = PublishSubject.create();
  public void signupClick() {
    signupClick.onNext(null);
  }

  // OUTPUTS
  private final BehaviorSubject<LoginReason> startLogin = BehaviorSubject.create();
  public @NonNull Observable<LoginReason> startLogin() {
    return startLogin;
  }

  private final BehaviorSubject<LoginReason> loginClickDefaultFlow = BehaviorSubject.create();
  public @NonNull Observable<LoginReason> loginClickDefaultFlow() {
    return loginClickDefaultFlow;
  }

  private final BehaviorSubject<Void> loginSuccessContextualFlow = BehaviorSubject.create();
  public @NonNull Observable<Void> loginSuccessContextualFlow() {
    return loginSuccessContextualFlow;
  }

  private final BehaviorSubject<Void> loginSuccessDefaultFlow = BehaviorSubject.create();
  public @NonNull Observable<Void> loginSuccessDefaultFlow() {
    return loginSuccessDefaultFlow;
  }

  private final BehaviorSubject<LoginReason> signupClickContextualFlow = BehaviorSubject.create();
  public @NonNull Observable<LoginReason> signupClickContextualFlow() {
    return signupClickContextualFlow;
  }

  BehaviorSubject<LoginReason> signupClickDefaultFlow = BehaviorSubject.create();
  public @NonNull Observable<LoginReason> signupClickDefaultFlow() {
    return signupClickDefaultFlow;
  }

  // ERRORS
  private final PublishSubject<FacebookException> facebookAuthorizationError = PublishSubject.create();
  public Observable<String> facebookAuthorizationError() {
    return facebookAuthorizationError
      .map(FacebookException::getLocalizedMessage);
  }

  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();

  @NonNull
  @Override
  public Observable<Pair<ErrorEnvelope.FacebookUser, LoginReason>> confirmFacebookSignupError() {
    return loginError
      .filter(ErrorEnvelope::isConfirmFacebookSignupError)
      .map(ErrorEnvelope::facebookUser)
      .compose(Transformers.combineLatestPair(loginReason));
  }
  @Override
  public Observable<String> missingFacebookEmailError() {
    return loginError
      .filter(ErrorEnvelope::isMissingFacebookEmailError)
      .map(ErrorEnvelope::errorMessage);
  }

  public final Observable<String> facebookInvalidAccessTokenError() {
    return loginError
      .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
      .map(ErrorEnvelope::errorMessage);
  }

  @Override
  public Observable<LoginReason> tfaChallenge() {
    return loginReason
      .compose(Transformers.takeWhen(loginError.filter(ErrorEnvelope::isTfaRequiredError)));
  }

  protected @Inject CurrentUser currentUser;
  protected @Inject ApiClientType client;

  Observable<LoginReason> loginReason;

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

    Observable<LoginReason> contextualLoginReason = loginReason
      .filter(LoginReason::isContextualFlow);

    Observable<LoginReason> defaultLoginReason = loginReason
      .filter(LoginReason::isDefaultFlow);

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

    addSubscription(facebookLoginSuccess.subscribe(envelope -> currentUser.login(envelope.user(), envelope.accessToken())));

    addSubscription(facebookLoginSuccess.subscribe(__ -> koala.trackFacebookLoginSuccess()));

    addSubscription(contextualLoginReason
      .compose(Transformers.takeWhen(loginClick))
      .subscribe(startLogin::onNext));

    addSubscription(defaultLoginReason
      .compose(Transformers.takeWhen(loginClick))
      .subscribe(startLogin::onNext));

    addSubscription(contextualLoginReason
      .compose(Transformers.takeWhen(facebookLoginSuccess))
      .subscribe(__ -> loginSuccessContextualFlow.onNext(null)));

    addSubscription(defaultLoginReason
      .compose(Transformers.takeWhen(facebookLoginSuccess))
      .subscribe(__ -> loginSuccessDefaultFlow.onNext(null)));

    addSubscription(contextualLoginReason
      .compose(Transformers.takeWhen(signupClick))
      .subscribe(signupClickContextualFlow::onNext));

    addSubscription(defaultLoginReason
      .compose(Transformers.takeWhen(signupClick))
      .subscribe(signupClickDefaultFlow::onNext));

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
