package com.kickstarter.viewmodels;

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
import com.kickstarter.libs.utils.EventContextValues.ContextTypeName;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.data.ActivityResult;
import com.kickstarter.ui.data.LoginReason;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.errors;
import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.values;

public interface LoginToutViewModel {

  interface Inputs {
    /** Call when the Login to Facebook button is clicked. */
    void facebookLoginClick(final @Nullable LoginToutActivity activity, final @NonNull List<String> facebookPermissions);

    /** Call when the login button is clicked. */
    void loginClick();

    /** Call when the signup button is clicked. */
    void signupClick();
  }

  interface Outputs {
    /** Emits when a user has successfully logged in; the login flow should finish with a result indicating success. */
    Observable<Void> finishWithSuccessfulResult();

    /** Emits when a user has failed to authenticate using Facebook. */
    Observable<String> showFacebookAuthorizationErrorDialog();

    /** Emits when the API was unable to create a new Facebook user. */
    Observable<String> showFacebookInvalidAccessTokenErrorToast();

    /** Emits when the API could not retrieve an email for the Facebook user. */
    Observable<String> showMissingFacebookEmailErrorToast();

    /** Emits when a login attempt is unauthorized. */
    Observable<String> showUnauthorizedErrorDialog();

    /** Emits a Facebook user and an access token string to confirm Facebook signup. */
    Observable<Pair<ErrorEnvelope.FacebookUser, String>> startFacebookConfirmationActivity();

    /** Emits when the login activity should be started. */
    Observable<Void> startLoginActivity();

    /** Emits when the signup activity should be started. */
    Observable<Void> startSignupActivity();

    /** Emits when a user has successfully logged in using Facebook, but has require two-factor authentication enabled. */
    Observable<Void> startTwoFactorChallenge();
  }

  final class ViewModel extends ActivityViewModel<LoginToutActivity> implements Inputs, Outputs {
    private CallbackManager callbackManager;
    private final CurrentUserType currentUser;
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      registerFacebookCallback();

      final Observable<Notification<AccessTokenEnvelope>> facebookAccessTokenEnvelope = this.facebookAccessToken
        .switchMap(this::loginWithFacebookAccessToken)
        .share();

      intent()
        .map(i -> i.getSerializableExtra(IntentKey.LOGIN_REASON))
        .ofType(LoginReason.class)
        .compose(bindToLifecycle())
        .subscribe(this.loginReason::onNext);

      activityResult()
        .compose(bindToLifecycle())
        .subscribe(r -> this.callbackManager.onActivityResult(r.requestCode(), r.resultCode(), r.intent()));

      activityResult()
        .filter(r -> r.isRequestCode(ActivityRequestCodes.LOGIN_FLOW))
        .filter(ActivityResult::isOk)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.finishWithSuccessfulResult.onNext(null));

      this.facebookAuthorizationError
        .compose(bindToLifecycle())
        .subscribe(this::clearFacebookSession);

      facebookAccessTokenEnvelope
        .compose(values())
        .compose(bindToLifecycle())
        .subscribe(envelope -> {
          this.currentUser.login(envelope.user(), envelope.accessToken());
          this.finishWithSuccessfulResult.onNext(null);
        });

      facebookAccessTokenEnvelope
        .compose(errors())
        .map(ErrorEnvelope::fromThrowable)
        .filter(ObjectUtils::isNotNull)
        .compose(bindToLifecycle())
        .subscribe(this.loginError::onNext);

      this.startFacebookConfirmationActivity = this.loginError
        .filter(ErrorEnvelope::isConfirmFacebookSignupError)
        .map(ErrorEnvelope::facebookUser)
        .compose(combineLatestPair(this.facebookAccessToken));

      this.startLoginActivity = this.loginClick;
      this.startSignupActivity = this.signupClick;

      this.loginReason
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(__ -> this.lake.trackLogInSignUpPageViewed());

      this.facebookLoginClick
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(__ -> {
          this.lake.trackFacebookLogInSignUpButtonClicked();
          this.lake.trackLoginOrSignUpCtaClicked(ContextTypeName.FACEBOOK.getContextName(), );
        });

      this.loginClick
        .compose(bindToLifecycle())
        .subscribe(__ -> this.lake.trackLogInButtonClicked());

      this.signupClick
        .compose(bindToLifecycle())
        .subscribe(__ -> this.lake.trackSignUpButtonClicked());
    }

    private void clearFacebookSession(final @NonNull FacebookException e) {
      LoginManager.getInstance().logOut();
    }

    private @NonNull Observable<Notification<AccessTokenEnvelope>> loginWithFacebookAccessToken(final @NonNull String fbAccessToken) {
      return this.client.loginWithFacebook(fbAccessToken)
        .materialize();
    }

    private void registerFacebookCallback() {
      this.callbackManager = CallbackManager.Factory.create();

      LoginManager.getInstance().registerCallback(this.callbackManager, new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(final @NonNull LoginResult result) {
          ViewModel.this.facebookAccessToken.onNext(result.getAccessToken().getToken());
        }

        @Override
        public void onCancel() {
          // continue
        }

        @Override
        public void onError(final @NonNull FacebookException error) {
          if (error instanceof FacebookAuthorizationException) {
            ViewModel.this.facebookAuthorizationError.onNext(error);
          }
        }
      });
    }

    @VisibleForTesting
    final PublishSubject<String> facebookAccessToken = PublishSubject.create();
    final PublishSubject<List<String>> facebookLoginClick = PublishSubject.create();
    private final PublishSubject<Void> loginClick = PublishSubject.create();
    @VisibleForTesting
    final PublishSubject<ErrorEnvelope> loginError = PublishSubject.create();
    private final PublishSubject<LoginReason> loginReason = PublishSubject.create();
    private final PublishSubject<Void> signupClick = PublishSubject.create();

    private final BehaviorSubject<FacebookException> facebookAuthorizationError = BehaviorSubject.create();
    private final BehaviorSubject<Void> finishWithSuccessfulResult = BehaviorSubject.create();
    private final Observable<Pair<ErrorEnvelope.FacebookUser, String>> startFacebookConfirmationActivity;
    private final Observable<Void> startLoginActivity;
    private final Observable<Void> startSignupActivity;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void facebookLoginClick(final @Nullable LoginToutActivity activity, final @NonNull List<String> facebookPermissions) {
      this.facebookLoginClick.onNext(facebookPermissions);
      if (activity != null) {
        LoginManager.getInstance().logInWithReadPermissions(activity, facebookPermissions);
      }
    }
    @Override public void loginClick() {
      this.loginClick.onNext(null);
    }
    @Override public void signupClick() {
      this.signupClick.onNext(null);
    }

    @Override public @NonNull Observable<Void> finishWithSuccessfulResult() {
      return this.finishWithSuccessfulResult;
    }
    @Override public @NonNull Observable<String> showFacebookAuthorizationErrorDialog() {
      return this.facebookAuthorizationError
        .map(FacebookException::getLocalizedMessage);
    }
    @Override public @NonNull Observable<String> showFacebookInvalidAccessTokenErrorToast() {
      return this.loginError
        .filter(ErrorEnvelope::isFacebookInvalidAccessTokenError)
        .map(ErrorEnvelope::errorMessage);
    }
    @Override public @NonNull Observable<String> showMissingFacebookEmailErrorToast() {
      return this.loginError
        .filter(ErrorEnvelope::isMissingFacebookEmailError)
        .map(ErrorEnvelope::errorMessage);
    }
    @Override public @NonNull Observable<String> showUnauthorizedErrorDialog() {
      return this.loginError
        .filter(ErrorEnvelope::isUnauthorizedError)
        .map(ErrorEnvelope::errorMessage);
    }
    @Override public @NonNull Observable<Pair<ErrorEnvelope.FacebookUser, String>> startFacebookConfirmationActivity() {
      return this.startFacebookConfirmationActivity;
    }
    @Override public @NonNull Observable<Void> startLoginActivity() {
      return this.startLoginActivity;
    }
    @Override public @NonNull Observable<Void> startSignupActivity() {
      return this.startSignupActivity;
    }
    @Override public @NonNull Observable<Void> startTwoFactorChallenge() {
      return this.loginError
        .filter(ErrorEnvelope::isTfaRequiredError)
        .map(__ -> null);
    }
  }
}
