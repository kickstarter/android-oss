package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.presenters.FacebookConfirmationPresenter;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.toolbars.LoginToolbar;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresPresenter(FacebookConfirmationPresenter.class)
public class FacebookConfirmationActivity extends BaseActivity<FacebookConfirmationPresenter> {
  protected @Bind(R.id.email) TextView emailTextView;
  protected @Bind(R.id.sign_up_with_facebook_toolbar) LoginToolbar signUpWithFacebookToolbar;

  protected @BindString(R.string.Sign_up_with_Facebook) String signUpWithFacebookString;

  private boolean forward;

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.facebook_confirmation_layout);
    ButterKnife.bind(this);
    signUpWithFacebookToolbar.setTitle(signUpWithFacebookString);

    forward = getIntent().getBooleanExtra(getString(R.string.intent_forward), false);
    final ErrorEnvelope.FacebookUser fbUser = getIntent().getParcelableExtra(getString(R.string.intent_facebook_user));
    emailTextView.setText(fbUser.email());

    // todo: errors and outputs

  }

  @OnClick(R.id.create_new_account_button)
  public void createNewAccountClick() {
    presenter.inputs.createNewAccountClick();
  }

  @OnClick(R.id.login_button)
  public void loginWithEmailClick() {
    final Intent intent = new Intent(this, LoginActivity.class);
    if (forward) {
      intent.putExtra(getString(R.string.intent_forward), true);
      startActivityForResult(intent,
        ActivityRequestCodes.FACEBOOK_CONFIRMATION_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
    } else {
      startActivity(intent);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }
}
