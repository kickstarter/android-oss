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
import com.kickstarter.viewmodels.errors.LoginToutViewModelErrors;
import com.kickstarter.viewmodels.inputs.LoginToutViewModelInputs;
import com.kickstarter.viewmodels.outputs.LoginToutViewModelOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.LoginToutActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class LoginToutViewModel extends ViewModel<LoginToutActivity> implements LoginToutViewModelInputs,
  LoginToutViewModelOutputs, LoginToutViewModelErrors {

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
  private final PublishSubject<String> reason = PublishSubject.create();

  // OUTPUTS
  private final PublishSubject<Void> facebookLoginSuccess = PublishSubject.create();
  public final Observable<Void> facebookLoginSuccess() {
    return facebookLoginSuccess.asObservable();
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

  @Inject CurrentUser currentUser;
  @Inject ApiClient client;

  public final LoginToutViewModelInputs inputs = this;
  public final LoginToutViewModelOutputs outputs = this;
  public final LoginToutViewModelErrors errors = this;

  public LoginToutViewModel() {
    final CallbackManager callbackManager = CallbackManager.Factory.create();
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

    addSubscription(activityResult
      .subscribe(r -> callbackManager.onActivityResult(r.requestCode, r.resultCode, r.intent))
    );

    addSubscription(facebookAccessToken
      .switchMap(this::loginWithFacebookAccessToken)
      .subscribe(this::facebookLoginSuccess)
    );

    addSubscription(facebookAuthorizationError
      .subscribe(this::clearFacebookSession)
    );
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);
    addSubscription(reason.take(1).subscribe(koala::trackLoginRegisterTout));

    addSubscription(loginError.subscribe(__ -> koala.trackLoginError()));

    addSubscription(facebookLoginSuccess.subscribe(__ -> koala.trackFacebookLoginSuccess()));

    addSubscription(missingFacebookEmailError()
      .mergeWith(facebookInvalidAccessTokenError())
      .mergeWith(facebookAuthorizationError())
      .subscribe(__ -> koala.trackFacebookLoginError()));
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

  public void facebookLoginSuccess(@NonNull final AccessTokenEnvelope envelope) {
    currentUser.login(envelope.user(), envelope.accessToken());
    facebookLoginSuccess.onNext(null);
  }

  private Observable<AccessTokenEnvelope> loginWithFacebookAccessToken(@NonNull final String fbAccessToken) {
    return client.loginWithFacebook(fbAccessToken)
      .compose(Transformers.pipeApiErrorsTo(loginError));
  }

  @Override
  public void reason(@Nullable final String r) {
    reason.onNext(r);
  }
}
