package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.presenters.TwoFactorPresenter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(TwoFactorPresenter.class)
public class TwoFactorActivity extends BaseActivity<TwoFactorPresenter> {
  public @InjectView(R.id.code) EditText codeEditText;
  public @InjectView(R.id.resend_button) Button resendButton;
  public @InjectView(R.id.login_button) Button loginButton;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.two_factor_layout);
    ButterKnife.inject(this);
  }

  public void setLoginEnabled(final boolean enabled) {
    loginButton.setEnabled(enabled);
  }

  public void resendButtonOnClick(final View v) {
    presenter.takeResendClick();
  }

  public void loginButtonOnClick(final View v) {
    presenter.takeLoginClick();
  }

  public void onSuccess() {
    if (forward()) {
      setResult(Activity.RESULT_OK);
      finish();
    } else {
      final Intent intent = new Intent(this, DiscoveryActivity.class)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
    }
  }

  public String email () {
    return getIntent().getExtras().getString(getString(R.string.intent_email));
  }

  public String password () {
    return getIntent().getExtras().getString(getString(R.string.intent_password));
  }

  public boolean forward () {
    return getIntent().getBooleanExtra(getString(R.string.intent_forward), false);
  }
}
