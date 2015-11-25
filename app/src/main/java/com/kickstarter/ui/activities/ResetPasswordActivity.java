package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.viewmodels.ResetPasswordViewModel;
import com.kickstarter.ui.toolbars.LoginToolbar;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(ResetPasswordViewModel.class)
public final class ResetPasswordActivity extends BaseActivity<ResetPasswordViewModel> {
  @Bind (R.id.email) EditText email;
  @Bind (R.id.reset_password_button) Button resetPasswordButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;

  @BindString(R.string.Forgot_your_password) String forgotPasswordString;
  @BindString(R.string.Sorry_we_do_not_know_that_email_address_try_again) String errorMessageString;
  @BindString(R.string.Oops) String errorTitleString;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.reset_password_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(forgotPasswordString);

    addSubscription(
      viewModel.outputs.resetSuccess()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> onResetSuccess())
    );

    addSubscription(
      viewModel.outputs.isFormSubmitting()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::setFormDisabled)
    );

    addSubscription(
      viewModel.outputs.isFormValid()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::setFormEnabled)
    );

    addSubscription(
      viewModel.errors.resetError()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> ViewUtils.showDialog(this, errorTitleString, errorMessageString))
    );
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @OnTextChanged(R.id.email)
  void onEmailTextChanged(@NonNull final CharSequence email) {
    viewModel.inputs.email(email.toString());
  }

  @OnClick(R.id.reset_password_button)
  public void resetButtonOnClick() {
    viewModel.inputs.resetPasswordClick();
  }

  private void onResetSuccess() {
    setFormEnabled(false);
    final Intent intent = new Intent(this, LoginActivity.class)
      .putExtra(getString(R.string.intent_confirm_reset_password), true)
      .putExtra(getString(R.string.intent_email), email.getText().toString());
    startActivityWithTransition(intent, R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  private void setFormEnabled(final boolean isEnabled) {
    resetPasswordButton.setEnabled(isEnabled);
  }

  private void setFormDisabled(final boolean isDisabled) {
    setFormEnabled(!isDisabled);
  }
}
