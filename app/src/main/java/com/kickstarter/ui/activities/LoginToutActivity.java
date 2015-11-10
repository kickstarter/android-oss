package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.presenters.LoginToutPresenter;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresPresenter(LoginToutPresenter.class)
public final class LoginToutActivity extends BaseActivity<LoginToutPresenter> {
  public static final String LOGIN_INTENT_BACK_PROJECT = "pledge";
  public static final String LOGIN_INTENT_GENERIC = "generic";
  public static final String LOGIN_INTENT_LOGIN_TAB = "login_tab";
  public static final String LOGIN_INTENT_MESSAGE_CREATOR = "new_message";
  public static final String LOGIN_INTENT_STAR_PROJECT = "star";

  @Bind(R.id.disclaimer_text_view) TextView disclaimerTextView;
  @Bind(R.id.login_button) Button loginButton;
  @Bind(R.id.sign_up_button) Button signupButton;
  @Bind(R.id.help_button) TextView helpButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;
  @BindString(R.string.Not_implemented_yet) String notImplementedYetString;
  @BindString(R.string.Log_in_or_sign_up) String loginOrSignUpString;

  private boolean forward;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_tout_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(loginOrSignUpString);

    forward = getIntent().getBooleanExtra(getString(R.string.intent_forward), false);
    //presenter.showLoginWithIntent(getIntent().getExtras().getString(getString(R.string.intent_login_type)));
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
  public void facebookLoginButtonClick() {
    displayToast(notImplementedYetString);
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
    if (requestCode != ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD) {
      return;
    }

    setResult(resultCode, intent);
    finish();
  }
}
