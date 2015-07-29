package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.presenters.LoginToutPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(LoginToutPresenter.class)
public class LoginToutActivity extends BaseActivity<LoginToutPresenter> {
  @InjectView(R.id.login_button) Button login_button;
  @InjectView(R.id.sign_up_button) Button signup_button;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    FacebookSdk.sdkInitialize(this.getApplicationContext());

    setContentView(R.layout.login_tout_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    Timber.d("onCreate, forward is " + intent.getBooleanExtra("forward", false));
    presenter.takeForwardFlag(intent.getBooleanExtra("forward", false));
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

  public void loginButtonOnClick(final View view) {
    presenter.takeLoginButtonClick();
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
    Timber.d("onActivityResult, requestCode is " + requestCode);
    if (requestCode != ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD) {
      return;
    }

    setResult(resultCode, intent);
    finish();
  }
}
