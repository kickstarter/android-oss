package com.kickstarter.ui.activities;

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

    Intent intent = getIntent();
    String email = intent.getExtras().getString("email");
    String password = intent.getExtras().getString("password");
    presenter.takeEmailAndPassword(email, password);
  }

  public void setLoginEnabled(final boolean enabled) {
    login_button.setEnabled(enabled);
  }

  public void resendButtonOnClick(final View v) {
    Timber.d("resendButtonOnClick");
  }

  public void loginButtonOnClick(final View v) {
    Timber.d("loginButtonOnClick");
    presenter.login();
  }
}
