package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.presenters.LoginPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(LoginPresenter.class)
public class LoginActivity extends BaseActivity<LoginPresenter> {
  public @InjectView(R.id.email) EditText email;
  public @InjectView(R.id.password) EditText password;
  public @InjectView(R.id.login_button) Button login_button;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    Timber.d("onCreate, forward is " + intent.getBooleanExtra("forward", false));
    presenter.takeForwardFlag(intent.getBooleanExtra("forward", false));
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Timber.d("onBackPressed %s", toString());

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void loginButtonOnClick(final View v) {
    Timber.d("login_button clicked");
    presenter.login();
  }

  public void setFormEnabled(final boolean enabled) {
    login_button.setEnabled(enabled);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
    Timber.d("onActivityResult, requestCode is " + requestCode);
    if (requestCode != ActivityRequestCodes.LOGIN_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD) {
      return;
    }

    setResult(resultCode, intent);
    finish();
  }
}
