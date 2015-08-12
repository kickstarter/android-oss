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
  public @InjectView(R.id.code) EditText code;
  public @InjectView(R.id.resend_button) Button resend_button;
  public @InjectView(R.id.login_button) Button login_button;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.two_factor_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    final String email = intent.getExtras().getString(getString(R.string.intent_email));
    final String password = intent.getExtras().getString(getString(R.string.intent_password));
    final boolean forward = intent.getBooleanExtra(getString(R.string.intent_forward), false);
    presenter.takeEmailAndPassword(email, password);
    presenter.takeForward(forward);
  }

  public void setLoginEnabled(final boolean enabled) {
    login_button.setEnabled(enabled);
  }

  public void resendButtonOnClick(final View v) {
    Timber.d("resendButtonOnClick");
    presenter.resend();
  }

  public void loginButtonOnClick(final View v) {
    Timber.d("loginButtonOnClick");
    presenter.login();
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
}
