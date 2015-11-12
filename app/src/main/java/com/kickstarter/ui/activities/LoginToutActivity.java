package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.presenters.LoginToutPresenter;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@RequiresPresenter(LoginToutPresenter.class)
public final class LoginToutActivity extends BaseActivity<LoginToutPresenter> {
  @Bind(R.id.disclaimer_text_view) TextView disclaimerTextView;
  @Bind(R.id.login_button) Button loginButton;
  @Bind(R.id.facebook_login_button) LoginButton facebookButton;
  @Bind(R.id.sign_up_button) Button signupButton;
  @Bind(R.id.help_button) TextView helpButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;

  @BindString(R.string.Not_implemented_yet) String notImplementedYetString;
  @BindString(R.string.Log_in_or_sign_up) String loginOrSignUpString;
  @BindString(R.string.Unable_to_login) String unableToLoginString;

  private CallbackManager callbackManager;
  private boolean forward;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_tout_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(loginOrSignUpString);

    forward = getIntent().getBooleanExtra(getString(R.string.intent_forward), false);

    addSubscription(
      presenter.errors.tfaChallenge()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> startTwoFactorActivity(true, forward))
    );

    addSubscription(
      errorMessages()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::displayToast)
    );
  }

  private Observable<String> errorMessages() {
    return presenter.errors.missingFacebookEmailError().map(ObjectUtils.coalesceWith(unableToLoginString))
      .mergeWith(presenter.errors.facebookInvalidAccessTokenError().map(ObjectUtils.coalesceWith(unableToLoginString)));
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

  @OnClick({R.id.disclaimer_text_view})
  public void disclaimerTextViewClick() {
    new LoginPopupMenu(this, helpButton).show();
  }

  @OnClick(R.id.facebook_login_button)
  public void facebookLoginClick() {
    callbackManager = CallbackManager.Factory.create();
    facebookButton.setReadPermissions("user_friends");
    facebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

      @Override
      public void onSuccess(@NonNull final LoginResult result) {
        Log.d("TEST", "onSuccess");
        presenter.facebookLoginResult(result);
      }

      @Override
      public void onCancel() {

      }

      @Override
      public void onError(@NonNull final FacebookException error) {
        Log.d("TEST", "onError");
      }
    });
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
    // this needs to be called. probably not always tho
    callbackManager.onActivityResult(requestCode, resultCode, intent);

    if (requestCode != ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD) {
      Log.d("TEST", "nope");
      return;
    }

    setResult(resultCode, intent);
    finish();
  }

  // todo: startTwoFactorActivity with Facebook access token
  public void startTwoFactorActivity(final boolean forward, final boolean isFacebookLogin) {
    final Intent intent = new Intent(this, TwoFactorActivity.class)
      .putExtra(getString(R.string.intent_facebook_login), isFacebookLogin);
    if (forward) {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
  }
}
