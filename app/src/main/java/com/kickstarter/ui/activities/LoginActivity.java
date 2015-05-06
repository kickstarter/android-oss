package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.User;
import com.kickstarter.presenters.LoginPresenter;
import com.kickstarter.services.ApiResponses.AccessTokenEnvelope;
import com.kickstarter.services.KickstarterClient;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.view.ViewObservable;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import timber.log.Timber;

@RequiresPresenter(LoginPresenter.class)
public class LoginActivity extends BaseActivity<LoginPresenter> {
  public @InjectView(R.id.email_address) TextView email_address;
  public @InjectView(R.id.password) TextView password;
  public @InjectView(R.id.login_button) Button login_button;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_layout);
    ButterKnife.inject(this);
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

  public void setFormEnabled(boolean enabled) {
    login_button.setEnabled(enabled);
    // TODO: figure this out
    // login_button.setBackgroundColor(getResources().getColor(enabled ? R.color.green : R.color.gray));
  }
}
