package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.ConfirmDialog;
import com.kickstarter.viewmodels.LoginViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(LoginViewModel.class)
public final class LoginActivity extends BaseActivity<LoginViewModel> {
  private ConfirmDialog confirmResetPasswordSuccessDialog;

  protected @Bind(R.id.email) EditText emailEditText;
  protected @Bind(R.id.forgot_your_password_text_view) TextView forgotPasswordTextView;
  protected @Bind(R.id.login_button) Button loginButton;
  protected @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;
  protected @Bind(R.id.password) EditText passwordEditText;

  protected @BindString(R.string.login_buttons_forgot_password_html) String forgotPasswordString;
  protected @BindString(R.string.forgot_password_we_sent_an_email_to_email_address_with_instructions_to_reset_your_password) String forgotPasswordSentEmailString;
  protected @BindString(R.string.login_errors_does_not_match) String loginDoesNotMatchString;
  protected @BindString(R.string.login_errors_unable_to_log_in) String unableToLoginString;
  protected @BindString(R.string.login_buttons_log_in) String loginString;
  protected @BindString(R.string.login_errors_title) String errorTitleString;

  @Inject KSString ksString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_layout);
    ((KSApplication) getApplication()).component().inject(this);
    ButterKnife.bind(this);
    this.loginToolbar.setTitle(this.loginString);
    this.forgotPasswordTextView.setText(Html.fromHtml(this.forgotPasswordString));

    errorMessages()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(e -> ViewUtils.showDialog(this, this.errorTitleString, e));

    this.viewModel.errors.tfaChallenge()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> startTwoFactorActivity());

    this.viewModel.outputs.loginSuccess()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> onSuccess());

    this.viewModel.outputs.prefillEmailFromPasswordReset()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.emailEditText::setText);

    this.viewModel.outputs.showResetPasswordSuccessDialog()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(showAndEmail -> {
        final boolean show = showAndEmail.first;
        final String email = showAndEmail.second;
        if (show) {
          resetPasswordSuccessDialog(email).show();
        } else {
          resetPasswordSuccessDialog(email).dismiss();
        }
      });

    this.viewModel.outputs.setLoginButtonIsEnabled()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setLoginButtonEnabled);
  }

  /**
   * Lazily creates a reset password success confirmation dialog and stores it in an instance variable.
   */
  private @NonNull ConfirmDialog resetPasswordSuccessDialog(final @NonNull String email) {
    if (this.confirmResetPasswordSuccessDialog == null) {
      final String message = this.ksString.format(this.forgotPasswordSentEmailString, "email", email);
      this.confirmResetPasswordSuccessDialog = new ConfirmDialog(this, null, message);

      this.confirmResetPasswordSuccessDialog
        .setOnDismissListener(__ -> this.viewModel.inputs.resetPasswordConfirmationDialogDismissed());
      this.confirmResetPasswordSuccessDialog
        .setOnCancelListener(__ -> this.viewModel.inputs.resetPasswordConfirmationDialogDismissed());
    }
    return this.confirmResetPasswordSuccessDialog;
  }

  private Observable<String> errorMessages() {
    return this.viewModel.errors.invalidLoginError()
      .map(ObjectUtils.coalesceWith(this.loginDoesNotMatchString))
      .mergeWith(this.viewModel.errors.genericLoginError()
        .map(ObjectUtils.coalesceWith(this.unableToLoginString))
      );
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final @Nullable Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);

    if (requestCode != ActivityRequestCodes.LOGIN_FLOW) {
      return;
    }

    setResult(resultCode, intent);
    finish();
  }

  @OnTextChanged(R.id.email)
  void onEmailTextChanged(final @NonNull CharSequence email) {
    this.viewModel.inputs.email(email.toString());
  }

  @OnTextChanged(R.id.password)
  void onPasswordTextChanged(final @NonNull CharSequence password) {
    this.viewModel.inputs.password(password.toString());
  }

  @OnClick(R.id.forgot_your_password_text_view)
  public void forgotYourPasswordTextViewClick() {
    final Intent intent = new Intent(this, ResetPasswordActivity.class);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.login_button)
  public void loginButtonOnClick() {
    this.viewModel.inputs.loginClick();
  }

  public void onSuccess() {
    setResult(Activity.RESULT_OK);
    finish();
  }

  public void setLoginButtonEnabled(final boolean enabled) {
    this.loginButton.setEnabled(enabled);
  }

  public void startTwoFactorActivity() {
    final Intent intent = new Intent(this, TwoFactorActivity.class)
      .putExtra(IntentKey.EMAIL, this.emailEditText.getText().toString())
      .putExtra(IntentKey.PASSWORD, this.passwordEditText.getText().toString());
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
