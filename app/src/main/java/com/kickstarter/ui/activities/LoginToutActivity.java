package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.presenters.LoginToutPresenter;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@RequiresPresenter(LoginToutPresenter.class)
public final class LoginToutActivity extends BaseActivity<LoginToutPresenter> {
  public static final String REASON_BACK_PROJECT = "pledge";
  public static final String REASON_GENERIC = "generic";
  public static final String REASON_LOGIN_TAB = "login_tab";
  public static final String REASON_MESSAGE_CREATOR = "new_message";
  public static final String REASON_STAR_PROJECT = "star";

  @Bind(R.id.disclaimer_text_view) TextView disclaimerTextView;
  @Bind(R.id.login_button) Button loginButton;
  @Bind(R.id.facebook_login_button) Button facebookButton;
  @Bind(R.id.sign_up_button) Button signupButton;
  @Bind(R.id.help_button) TextView helpButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;

  @BindString(R.string.Not_implemented_yet) String notImplementedYetString;
  @BindString(R.string.Log_in_or_sign_up) String loginOrSignUpString;
  @BindString(R.string.Unable_to_login) String unableToLoginString;
  @BindString(R.string.intent_login_type) String intentLoginTypeString;
  @BindString(R.string.Oops) String errorTitleString;

  private CallbackManager callbackManager;
  private boolean forward;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_tout_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(loginOrSignUpString);

    forward = getIntent().getBooleanExtra(getString(R.string.intent_forward), false);

    presenter.inputs.reason(getIntent().getStringExtra(intentLoginTypeString));

    callbackManager = CallbackManager.Factory.create();
    LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
      @Override
      public void onSuccess(@NonNull final LoginResult result) {
        presenter.inputs.facebookAccessToken(result.getAccessToken().getToken());
      }

      @Override
      public void onCancel() {
        // continue
      }

      @Override
      public void onError(@NonNull final FacebookException error) {
        if (error instanceof FacebookAuthorizationException) {
          presenter.errors.facebookAuthorizationException(error);
        }
      }
    });

    addSubscription(
      presenter.errors.confirmFacebookSignupError()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::startFacebookConfirmationActivity)
    );

    addSubscription(
      presenter.errors.tfaChallenge()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> startTwoFactorActivity(true))
    );

    addSubscription(
      errorMessages()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::displayToast)
    );

    addSubscription(
      presenter.outputs.facebookLoginSuccess()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> {
          onSuccess(forward);
        })
    );
  }

  private Observable<String> errorMessages() {
    return presenter.errors.missingFacebookEmailError()
      .map(ObjectUtils.coalesceWith(unableToLoginString))
      .mergeWith(
        presenter.errors.facebookInvalidAccessTokenError()
          .map(ObjectUtils.coalesceWith(unableToLoginString))
      );
  }

  @Override
  protected void onResume() {
    super.onResume();

    /*
     * Temporary Facebook testing - logs 'install' and 'app activate' App Events.
     * This hits the Facebook API, we can remove it once login is working.
    */
    AppEventsLogger.activateApp(this);
  }

  @Override
  protected void onPause() {
    super.onPause();

    /*
     * Temporary Facebook testing - logs 'app deactivate' App Events.
     * This hits the Facebook API, we can remove it once login is working.
    */
    AppEventsLogger.deactivateApp(this);
  }

  public void handleFacebookAuthorizationError(@NonNull final FacebookException e) {
    displayDialog(errorTitleString, e.getLocalizedMessage());
    LoginManager.getInstance().logOut();
  }

  @OnClick({R.id.disclaimer_text_view})
  public void disclaimerTextViewClick() {
    new LoginPopupMenu(this, helpButton).show();
  }

  @OnClick(R.id.facebook_login_button)
  public void facebookLoginClick() {
    LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList(getResources()
      .getStringArray(R.array.facebook_permissions_array))
    );
  }

  @OnClick(R.id.login_button)
  public void loginButtonClick() {
    final Intent intent = new Intent(this, LoginActivity.class);
    if (forward) {
      intent.putExtra(getString(R.string.intent_forward), true);
      startActivityForResult(intent,
        ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.sign_up_button)
  public void signupButtonClick() {
    final Intent intent = new Intent(this, SignupActivity.class);
    if (forward) {
      intent.putExtra(getString(R.string.intent_forward), true);
      startActivityForResult(intent,
        ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    callbackManager.onActivityResult(requestCode, resultCode, intent);

    if (requestCode != ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD) {
      return;
    }

    setResult(resultCode, intent);
    finish();
  }

  public void onSuccess(final boolean forward) {
    if (forward) {
      setResult(Activity.RESULT_OK);
      finish();
    } else {
      final Intent intent = new Intent(this, DiscoveryActivity.class)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
    }
  }

  public void startFacebookConfirmationActivity(@NonNull final ErrorEnvelope.FacebookUser facebookUser) {
    final Intent intent = new Intent(this, FacebookConfirmationActivity.class)
      .putExtra(getString(R.string.intent_forward), forward)
      .putExtra(getString(R.string.intent_facebook_user), facebookUser);
    if (forward) {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_FACEBOOK_CONFIRMATION_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startTwoFactorActivity(final boolean isFacebookLogin) {
    final Intent intent = new Intent(this, TwoFactorActivity.class)
      .putExtra(getString(R.string.intent_facebook_login), isFacebookLogin)
      .putExtra(getString(R.string.intent_forward), forward)
      .putExtra(getString(R.string.intent_facebook_token), AccessToken.getCurrentAccessToken().getToken());
    if (forward) {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
