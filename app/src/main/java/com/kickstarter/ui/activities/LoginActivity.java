package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.viewmodels.LoginViewModel;
import com.kickstarter.ui.toolbars.LoginToolbar;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(LoginViewModel.class)
public final class LoginActivity extends BaseActivity<LoginViewModel> {
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
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_layout);
    ((KSApplication) getApplication()).component().inject(this);
    ButterKnife.bind(this);
    loginToolbar.setTitle(loginString);
    forgotPasswordTextView.setText(Html.fromHtml(forgotPasswordString));

    final Intent intent = getIntent();
    final boolean forward = intent.getBooleanExtra(IntentKey.FORWARD, false);

    errorMessages()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(e -> ViewUtils.showDialog(this, errorTitleString, e));

    viewModel.errors.tfaChallenge()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startTwoFactorActivity(forward));

    viewModel.outputs.loginSuccess()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> onSuccess(forward));

    final boolean confirmResetPassword = getIntent().getBooleanExtra(IntentKey.CONFIRM_RESET_PASSWORD, false);
    if (confirmResetPassword) {
      final String email = getIntent().getExtras().getString(IntentKey.EMAIL);
      final String message = ksString.format(forgotPasswordSentEmailString, "email", email);
      ViewUtils.showDialog(this, null, message);
      emailEditText.setText(email);
    }
  }

  private Observable<String> errorMessages() {
    return viewModel.errors.invalidLoginError()
      .map(ObjectUtils.coalesceWith(loginDoesNotMatchString))
      .mergeWith(viewModel.errors.genericLoginError()
        .map(ObjectUtils.coalesceWith(unableToLoginString))
      );
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    if (requestCode != ActivityRequestCodes.LOGIN_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD) {
      return;
    }

    setResult(resultCode, intent);
    finish();
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

  @OnTextChanged(R.id.password)
  void onPasswordTextChanged(@NonNull final CharSequence password) {
    viewModel.inputs.password(password.toString());
  }

  @OnClick(R.id.forgot_your_password_text_view)
  public void forgotYourPasswordTextViewClick() {
    final Intent intent = new Intent(this, ResetPasswordActivity.class);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.login_button)
  public void loginButtonOnClick() {
    viewModel.inputs.loginClick();
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

  public void setFormEnabled(final boolean enabled) {
    loginButton.setEnabled(enabled);
  }

  public void startTwoFactorActivity(final boolean forward) {
    final Intent intent = new Intent(this, TwoFactorActivity.class)
      .putExtra(IntentKey.EMAIL, emailEditText.getText().toString())
      .putExtra(IntentKey.PASSWORD, passwordEditText.getText().toString())
      .putExtra(IntentKey.FORWARD, forward);
    if (forward) {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
