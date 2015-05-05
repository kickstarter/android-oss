package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

public class LoginToutActivity extends BaseActivity {
  @InjectView(R.id.login_button) Button login_button;
  @InjectView(R.id.sign_up_button) Button signup_button;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    FacebookSdk.sdkInitialize(this.getApplicationContext());

    setContentView(R.layout.login_tout_layout);
    ButterKnife.inject(this);
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

  public void loginButtonOnClick(final View v) {
    Timber.d("login_button clicked");

    Intent intent = new Intent(this, LoginActivity.class);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
