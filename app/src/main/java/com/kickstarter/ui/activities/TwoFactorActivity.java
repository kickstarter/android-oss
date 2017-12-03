package com.kickstarter.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.viewmodels.TwoFactorViewModel;

import butterknife.BindView;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(TwoFactorViewModel.class)
public final class TwoFactorActivity extends BaseActivity<TwoFactorViewModel> {
  public @BindView(R.id.code) EditText codeEditText;
  public @BindView(R.id.resend_button) Button resendButton;
  public @BindView(R.id.login_button) Button loginButton;
  public @BindView(R.id.login_toolbar) LoginToolbar loginToolbar;

  @BindString(R.string.two_factor_error_message) String codeMismatchString;
  @BindString(R.string.login_errors_unable_to_log_in) String unableToLoginString;
  @BindString(R.string.two_factor_title) String verifyString;
  @BindString(R.string.login_errors_title) String errorTitleString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.two_factor_layout);
    ButterKnife.bind(this);
    this.loginToolbar.setTitle(this.verifyString);

    this.viewModel.outputs.tfaSuccess()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> onSuccess());

    this.viewModel.outputs.formSubmitting()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setFormDisabled);

    this.viewModel.outputs.formIsValid()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setFormEnabled);

    errorMessages()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(e -> ViewUtils.showDialog(this, this.errorTitleString, e));
  }

  private Observable<String> errorMessages() {
    return this.viewModel.errors.tfaCodeMismatchError().map(__ -> this.codeMismatchString)
      .mergeWith(this.viewModel.errors.genericTfaError().map(__ -> this.unableToLoginString));
  }

  @OnTextChanged(R.id.code)
  public void codeEditTextOnTextChanged(final @NonNull CharSequence code) {
    this.viewModel.inputs.code(code.toString());
  }

  @OnClick(R.id.resend_button)
  public void resendButtonOnClick() {
    this.viewModel.inputs.resendClick();
  }

  @OnClick(R.id.login_button)
  public void loginButtonOnClick() {
    this.viewModel.inputs.loginClick();
  }

  public void onSuccess() {
    setResult(Activity.RESULT_OK);
    finish();
  }

  public void setFormEnabled(final boolean enabled) {
    this.loginButton.setEnabled(enabled);
  }

  public void setFormDisabled(final boolean disabled) {
    setFormEnabled(!disabled);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
