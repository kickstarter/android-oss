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
  public @InjectView(R.id.submit_button) Button submit_button;
  private String email;
  private String password;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    email = intent.getExtras().getString("email");
    password = intent.getExtras().getString("password");

    setContentView(R.layout.two_factor_layout);
    ButterKnife.inject(this);
  }

  public void setSubmitEnabled(final boolean enabled) {
    submit_button.setEnabled(enabled);
  }

  public void resendButtonOnClick(final View v) {
    Timber.d("resendButtonOnClick");
  }

  public void submitButtonOnClick(final View v) {
    Timber.d("submitButtonOnClick");
    presenter.submit();
  }
}
