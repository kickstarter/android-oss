package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.presenters.ForgotPasswordPresenter;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.GenericDialogAlert;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.android.schedulers.AndroidSchedulers;

@RequiresPresenter(ForgotPasswordPresenter.class)
public final class ForgotPasswordActivity extends BaseActivity<ForgotPasswordPresenter> {
  @Bind (R.id.email) EditText email;
  @Bind (R.id.reset_password_button) Button resetPasswordButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;

  @BindString(R.string.Forgot_your_password) String forgotPasswordString;
  @BindString(R.string.Sorry_we_do_not_know_that_email_address_try_again) String errorMessageString;
  @BindString(R.string.Oops) String errorTitleString;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.forgot_password_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(forgotPasswordString);

    addSubscription(
      presenter.outputs.resetSuccess()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> onResetSuccess())
    );

    addSubscription(
      presenter.outputs.formSubmitting()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::setFormDisabled)
    );

    addSubscription(
      presenter.outputs.formIsValid()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::setFormEnabled)
    );

    addSubscription(
      presenter.errors.resetError()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> displayError())
    );
  }

  @OnTextChanged(R.id.email)
  void onEmailTextChanged(@NonNull final CharSequence email) {
    presenter.inputs.email(email.toString());
  }

  @OnClick(R.id.reset_password_button)
  public void resetButtonOnClick() {
    presenter.inputs.resetPasswordClick();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void onResetSuccess() {
    final Intent intent = new Intent(this, LoginActivity.class)
      .putExtra(getString(R.string.intent_confirm_reset_password), true)
      .putExtra(getString(R.string.intent_email), email.getText().toString());
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void setFormEnabled(final boolean enabled) {
    resetPasswordButton.setEnabled(enabled);
  }

  public void setFormDisabled(final boolean disabled) {
    setFormEnabled(!disabled);
  }

  private void displayError() {
    final GenericDialogAlert alert = new GenericDialogAlert(this);
    alert.show();
    alert.setTitleText(errorTitleString);
    alert.setMessage(errorMessageString);
  }
}
