package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.viewmodels.TwoFactorViewModel;
import com.kickstarter.ui.toolbars.LoginToolbar;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(TwoFactorViewModel.class)
public final class TwoFactorActivity extends BaseActivity<TwoFactorViewModel> {
  public @Bind(R.id.code) EditText codeEditText;
  public @Bind(R.id.resend_button) Button resendButton;
  public @Bind(R.id.login_button) Button loginButton;
  public @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;

  @BindString(R.string.two_factor_error_message) String codeMismatchString;
  @BindString(R.string.login_errors_unable_to_log_in) String unableToLoginString;
  @BindString(R.string.two_factor_title) String verifyString;
  @BindString(R.string.login_errors_title) String errorTitleString;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.two_factor_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(verifyString);

    viewModel.outputs.tfaSuccess()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> onSuccess());

    viewModel.outputs.formSubmitting()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setFormDisabled);

    viewModel.outputs.formIsValid()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setFormEnabled);

    errorMessages()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(e -> ViewUtils.showDialog(this, errorTitleString, e));
  }

  private Observable<String> errorMessages() {
    return viewModel.errors.tfaCodeMismatchError().map(ObjectUtils.coalesceWith(codeMismatchString))
      .mergeWith(viewModel.errors.genericTfaError().map(__ -> unableToLoginString));
  }

  @OnTextChanged(R.id.code)
  public void codeEditTextOnTextChanged(@NonNull final CharSequence code) {
    viewModel.inputs.code(code.toString());
  }

  public boolean forward() {
    return getIntent().getBooleanExtra(IntentKey.FORWARD, false);
  }

  @OnClick(R.id.resend_button)
  public void resendButtonOnClick() {
    viewModel.inputs.resendClick();
  }

  @OnClick(R.id.login_button)
  public void loginButtonOnClick() {
    viewModel.inputs.loginClick();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void onSuccess() {
    if (forward()) {
      setResult(Activity.RESULT_OK);
      finish();
    } else {
      final Intent intent = new Intent(this, DiscoveryActivity.class)
        .setAction(Intent.ACTION_MAIN)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);
    }
  }

  public void setFormEnabled(final boolean enabled) {
    loginButton.setEnabled(enabled);
  }

  public void setFormDisabled(final boolean disabled) {
    setFormEnabled(!disabled);
  }
}
