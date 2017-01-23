package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.data.ActivityResult;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.viewmodels.inputs.LoginToutViewModelInputs;
import com.kickstarter.viewmodels.outputs.LoginToutViewModelOutputs;

import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class LoginToutViewModel extends ActivityViewModel<LoginToutActivity> implements LoginToutViewModelInputs,
  LoginToutViewModelOutputs {
  private CallbackManager callbackManager;
  private final CurrentUserType currentUser;
  private final ApiClientType client;

  public LoginToutViewModel(final @NonNull Environment environment) {
    super(environment);

    client = environment.apiClient();
    currentUser = environment.currentUser();

    registerFacebookCallback();

    final Observable<AccessTokenEnvelope> facebookSuccessTokenEnvelope = facebookAccessToken
      .switchMap(this::loginWithFacebookAccessToken)
      .share();

    intent()
      .map(i -> i.getSerializableExtra(IntentKey.LOGIN_REASON))
      .ofType(LoginReason.class)
      .compose(bindToLifecycle())
      .subscribe(loginReason::onNext);

    activityResult()
      .compose(bindToLifecycle())
      .subscribe(r -> callbackManager.onActivityResult(r.requestCode(), r.resultCode(), r.intent()));

    activityResult()
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

    startFacebookConfirmationActivity = loginError
      .filter(ErrorEnvelope::isConfirmFacebookSignupError)
      .map(ErrorEnvelope::facebookUser)
      .compose(Transformers.combineLatestPair(facebookAccessToken));

    startLoginActivity = loginClick;
    startSignupActivity = signupClick;

    loginReason.take(1)
      .compose(bindToLifecycle())
      .subscribe(koala::trackLoginRegisterTout);

    loginError
      .compose(bindToLifecycle())
      .subscribe(__ -> koala.trackLoginError());

    showMissingFacebookEmailErrorToast()
      .mergeWith(showFacebookInvalidAccessTokenErrorToast())
      .mergeWith(showFacebookAuthorizationErrorDialog())
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

  private final PublishSubject<String> facebookAccessToken = PublishSubject.create();
  private final PublishSubject<Void> loginClick = PublishSubject.create();
  private final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
  private final PublishSubject<LoginReason> loginReason = PublishSubject.create();
  private final PublishSubject<Void> signupClick = PublishSubject.create();

  private final BehaviorSubject<FacebookException> facebookAuthorizationError = BehaviorSubject.create();
  private final BehaviorSubject<Void> finishWithSuccessfulResult = BehaviorSubject.create();
  private final Observable<Pair<ErrorEnvelope.FacebookUser, String>> startFacebookConfirmationActivity;
  private final Observable<Void> startLoginActivity;
  private final Observable<Void> startSignupActivity;

  public final LoginToutViewModelInputs inputs = this;
  public final LoginToutViewModelOutputs outputs = this;

  @Override public void facebookLoginClick(final @NonNull LoginToutActivity activity, final @NonNull List<String> facebookPermissions) {
    LoginManager.getInstance().logInWithReadPermissions(activity, facebookPermissions);
  }
  @Override public void loginClick() {
    loginClick.onNext(null);
  }
  @Override public void signupClick() {
    signupClick.onNext(null);
  }

  @Override public @NonNull Observable<Void> finishWithSuccessfulResult() {
    return finishWithSuccessfulResult;
  }
  @Override public @NonNull Observable<String> showFacebookAuthorizationErrorDialog() {
    return facebookAuthorizationError
      .map(FacebookException::getLocalizedMessage);
  }
  @Override public @NonNull Observable<String> showFacebookInvalidAccessTokenErrorToast() {
    return loginError
      .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
      .map(ErrorEnvelope::errorMessage);
  }
  @Override public @NonNull Observable<String> showMissingFacebookEmailErrorToast() {
    return loginError
      .filter(ErrorEnvelope::isMissingFacebookEmailError)
      .map(ErrorEnvelope::errorMessage);
  }
  @Override public @NonNull Observable<String> showUnauthorizedErrorDialog() {
    return loginError
      .filter(ErrorEnvelope::isUnauthorizedError)
      .map(ErrorEnvelope::errorMessage);
  }
  @Override public @NonNull Observable<Pair<ErrorEnvelope.FacebookUser, String>> startFacebookConfirmationActivity() {
    return startFacebookConfirmationActivity;
  }
  @Override public @NonNull Observable<Void> startLoginActivity() {
    return startLoginActivity;
  }
  @Override public @NonNull Observable<Void> startSignupActivity() {
    return startSignupActivity;
  }
  @Override public @NonNull Observable<Void> startTwoFactorChallenge() {
    return loginError
      .filter(ErrorEnvelope::isTfaRequiredError)
      .map(__ -> null);
  }
}
