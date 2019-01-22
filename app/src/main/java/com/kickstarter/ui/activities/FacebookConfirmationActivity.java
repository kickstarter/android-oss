package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.viewmodels.FacebookConfirmationViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromRight;
import static com.kickstarter.libs.utils.TransitionUtils.transition;

@RequiresActivityViewModel(FacebookConfirmationViewModel.ViewModel.class)
public class FacebookConfirmationActivity extends BaseActivity<FacebookConfirmationViewModel.ViewModel> {
  protected @Bind(R.id.email) TextView emailTextView;
  protected @Bind(R.id.help_button) TextView helpButton;
  protected @Bind(R.id.sign_up_with_facebook_toolbar) LoginToolbar signUpWithFacebookToolbar;
  protected @Bind(R.id.newsletter_switch) SwitchCompat newsletterSwitch;

  protected @BindString(R.string.facebook_confirmation_navbar_title) String signUpWithFacebookString;
  protected @BindString(R.string.signup_error_title) String errorTitleString;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.facebook_confirmation_layout);
    ButterKnife.bind(this);
    this.signUpWithFacebookToolbar.setTitle(this.signUpWithFacebookString);

    this.viewModel.outputs.prefillEmail()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::prefillEmail);

    this.viewModel.outputs.signupSuccess()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> onSuccess());

    this.viewModel.outputs.sendNewslettersIsChecked()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(b -> SwitchCompatUtils.setCheckedWithoutAnimation(this.newsletterSwitch, b));

    this.viewModel.outputs.signupError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(e -> ViewUtils.showDialog(this, this.errorTitleString, e));

    RxView.clicks(this.newsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.sendNewslettersClick(this.newsletterSwitch.isChecked()));
  }

  @OnClick(R.id.create_new_account_button)
  public void createNewAccountClick() {
    this.viewModel.inputs.createNewAccountClick();
  }

  @OnClick(R.id.login_button)
  public void loginWithEmailClick() {
    final Intent intent = new Intent(this, LoginActivity.class);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    transition(this, slideInFromRight());
  }

  public void onSuccess() {
    setResult(Activity.RESULT_OK);
    finish();
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  private void prefillEmail(final @NonNull String email) {
    this.emailTextView.setText(email);
  }
}
