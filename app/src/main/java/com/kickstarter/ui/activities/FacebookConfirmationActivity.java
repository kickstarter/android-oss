package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;
import com.kickstarter.viewmodels.FacebookConfirmationViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(FacebookConfirmationViewModel.class)
public class FacebookConfirmationActivity extends BaseActivity<FacebookConfirmationViewModel> {
  protected @Bind(R.id.email) TextView emailTextView;
  protected @Bind(R.id.help_button) TextView helpButton;
  protected @Bind(R.id.sign_up_with_facebook_toolbar) LoginToolbar signUpWithFacebookToolbar;
  protected @Bind(R.id.newsletter_switch) SwitchCompat newsletterSwitch;

  protected @BindString(R.string.facebook_confirmation_navbar_title) String signUpWithFacebookString;
  protected @BindString(R.string.signup_error_title) String errorTitleString;

  private boolean forward;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.facebook_confirmation_layout);
    ButterKnife.bind(this);
    signUpWithFacebookToolbar.setTitle(signUpWithFacebookString);

    final Intent intent = getIntent();
    forward = intent.getBooleanExtra(IntentKey.FORWARD, false);

    final ErrorEnvelope.FacebookUser fbUser = intent.getParcelableExtra(IntentKey.FACEBOOK_USER);
    emailTextView.setText(fbUser.email());

    final String fbAccessToken = intent.getStringExtra(IntentKey.FACEBOOK_TOKEN);
    viewModel.inputs.fbAccessToken(fbAccessToken);

    viewModel.outputs.signupSuccess()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> onSuccess(forward));

    viewModel.outputs.sendNewslettersIsChecked()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(b -> SwitchCompatUtils.setCheckedWithoutAnimation(newsletterSwitch, b));

    viewModel.errors.signupError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(e -> ViewUtils.showDialog(this, errorTitleString, e));

    RxView.clicks(newsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> viewModel.inputs.sendNewslettersClick(newsletterSwitch.isChecked()));
  }

  @OnClick(R.id.create_new_account_button)
  public void createNewAccountClick() {
    viewModel.inputs.createNewAccountClick();
  }

  @OnClick(R.id.disclaimer)
  public void disclaimerClick() {
    new LoginPopupMenu(this, helpButton).show();
  }

  @OnClick(R.id.login_button)
  public void loginWithEmailClick() {
    final Intent intent = new Intent(this, LoginActivity.class);
    if (forward) {
      intent.putExtra(IntentKey.FORWARD, true);
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
