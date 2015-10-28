package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;

import com.facebook.appevents.AppEventsLogger;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.presenters.LoginToutPresenter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresPresenter(LoginToutPresenter.class)
public class LoginToutActivity extends BaseActivity<LoginToutPresenter> {
  @Bind(R.id.login_button) Button loginButton;
  @Bind(R.id.sign_up_button) Button signupButton;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_tout_layout);
    ButterKnife.bind(this);

    presenter.takeForward(getIntent().getBooleanExtra(getString(R.string.intent_forward), false));
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

  @OnClick(R.id.login_button)
  public void loginButtonOnClick() {
    presenter.takeLoginButtonClick();
  }

  @OnClick(R.id.sign_up_button)
  public void signupButtonOnClick() {
    final Intent intent = new Intent(this, SignupActivity.class);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startLoginActivity(final boolean forward) {
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

  @OnClick(R.id.help_button)
  public void startHelpActivity() {
    // todo: hook this up to a dialog or spinner
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
