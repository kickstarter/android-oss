package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;
import com.kickstarter.viewmodels.SignupViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(SignupViewModel.class)
public final class SignupActivity extends BaseActivity<SignupViewModel> {
  @Bind(R.id.full_name) EditText nameEditText;
  @Bind(R.id.email) EditText emailEditText;
  @Bind(R.id.help_button) TextView helpButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;
  @Bind(R.id.password) EditText passwordEditText;
  @Bind(R.id.signup_button) Button signupButton;
  @Bind(R.id.newsletter_switch) SwitchCompat newsletterSwitch;
  @Bind(R.id.disclaimer) TextView disclaimerTextView;

  @BindString(R.string.signup_button) String signUpString;
  @BindString(R.string.signup_error_title) String errorTitleString;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.signup_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(signUpString);

    final boolean forward = getIntent().getBooleanExtra(IntentKey.FORWARD, false);

    viewModel.outputs.signupSuccess()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> onSuccess(forward));

    viewModel.outputs.formSubmitting()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setFormDisabled);

    viewModel.outputs.formIsValid()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setFormEnabled);

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

  @OnClick(R.id.disclaimer)
  public void disclaimerClick() {
   new LoginPopupMenu(this, helpButton).show();
  }

  @OnTextChanged(R.id.full_name)
  void onNameTextChanged(@NonNull final CharSequence fullName) {
    viewModel.inputs.fullName(fullName.toString());
  }

  @OnTextChanged(R.id.email)
  void onEmailTextChanged(@NonNull final CharSequence email) {
    viewModel.inputs.email(email.toString());
  }

  @OnTextChanged(R.id.password)
  void onPasswordTextChange(@NonNull final CharSequence password) {
    viewModel.inputs.password(password.toString());
  }

  @OnClick(R.id.signup_button)
  public void signupButtonOnClick() {
    viewModel.inputs.signupClick();
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

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void setFormEnabled(final boolean enabled) {
    signupButton.setEnabled(enabled);
  }

  public void setFormDisabled(final boolean disabled) {
    setFormEnabled(!disabled);
  }
}
