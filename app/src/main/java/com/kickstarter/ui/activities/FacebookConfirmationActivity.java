package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.presenters.FacebookConfirmationPresenter;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

@RequiresPresenter(FacebookConfirmationPresenter.class)
public class FacebookConfirmationActivity extends BaseActivity<FacebookConfirmationPresenter> {
  protected @Bind(R.id.email) TextView emailTextView;
  protected @Bind(R.id.help_button) TextView helpButton;
  protected @Bind(R.id.sign_up_with_facebook_toolbar) LoginToolbar signUpWithFacebookToolbar;
  protected @Bind(R.id.newsletter_switch) SwitchCompat newsletterSwitch;

  protected @BindString(R.string.Sign_up_with_Facebook) String signUpWithFacebookString;
  protected @BindString(R.string.Sign_up_error) String errorTitleString;

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

    final String fbAccessToken = getIntent().getStringExtra(getString(R.string.intent_facebook_token));
    presenter.inputs.fbAccessToken(fbAccessToken);

    addSubscription(
      presenter.outputs.signupSuccess()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> onSuccess(forward))
    );

    addSubscription(
      presenter.errors.signupError()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(e -> ViewUtils.showDialog(this, errorTitleString, e))
    );

    addSubscription(
      RxCompoundButton.checkedChanges(newsletterSwitch)
        .subscribe(presenter.inputs::sendNewsletters)
    );
  }

  @OnClick(R.id.create_new_account_button)
  public void createNewAccountClick() {
    presenter.inputs.createNewAccountClick();
  }

  @OnClick(R.id.disclaimer)
  public void disclaimerClick() {
    new LoginPopupMenu(this, helpButton).show();
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

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
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
