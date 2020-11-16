package com.kickstarter.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.LoginHelper;
import com.kickstarter.libs.utils.SwitchCompatUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.extensions.ActivityExtKt;
import com.kickstarter.ui.fragments.Callbacks;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;
import com.kickstarter.viewmodels.SignupViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(SignupViewModel.ViewModel.class)
public final class SignupActivity extends BaseActivity<SignupViewModel.ViewModel> {
  @Bind(R.id.name) EditText nameEditText;
  @Bind(R.id.email) EditText emailEditText;
  @Bind(R.id.help_button) TextView helpButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;
  @Bind(R.id.password) EditText passwordEditText;
  @Bind(R.id.signup_button) Button signupButton;
  @Bind(R.id.newsletter_switch) SwitchCompat newsletterSwitch;
  @Bind(R.id.disclaimer) TextView disclaimerTextView;

  @BindString(R.string.signup_button) String signUpString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.signup_layout);
    ButterKnife.bind(this);
    this.loginToolbar.setTitle(this.signUpString);

    this.viewModel.outputs.signupSuccess()
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

    this.viewModel.outputs.sendNewslettersIsChecked()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(b -> SwitchCompatUtils.setCheckedWithoutAnimation(this.newsletterSwitch, b));

    this.viewModel.outputs.errorString()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(e -> ViewUtils.showDialog(this, null, e));

    this.viewModel.outputs.showInterstitialFragment()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(envelope -> LoginHelper.INSTANCE.showInterstitialFragment(
        this.getSupportFragmentManager(),
        envelope,
        R.id.login_view_id,
        new Callbacks() {
          @Override
          public void onDismiss() {
            SignupActivity.this.onSuccess();
          }
        })
      );

    RxView.clicks(this.newsletterSwitch)
      .skip(1)
      .compose(bindToLifecycle())
      .subscribe(__ -> this.viewModel.inputs.sendNewslettersClick(this.newsletterSwitch.isChecked()));
  }

  @OnClick(R.id.disclaimer)
  public void disclaimerClick() {
    new LoginPopupMenu(this, this.helpButton).show();
  }

  @OnTextChanged(R.id.name)
  void onNameTextChanged(final @NonNull CharSequence name) {
    this.viewModel.inputs.name(name.toString());
  }

  @OnTextChanged(R.id.email)
  void onEmailTextChanged(final @NonNull CharSequence email) {
    this.viewModel.inputs.email(email.toString());
  }

  @OnTextChanged(R.id.password)
  void onPasswordTextChange(final @NonNull CharSequence password) {
    this.viewModel.inputs.password(password.toString());
  }

  @OnClick(R.id.signup_button)
  public void signupButtonOnClick() {
    this.viewModel.inputs.signupClick();
    ActivityExtKt.hideKeyboard(this);
  }

  public void onSuccess() {
    setResult(Activity.RESULT_OK);
    finish();
  }

  public void setFormEnabled(final boolean enabled) {
    this.signupButton.setEnabled(enabled);
  }

  public void setFormDisabled(final boolean disabled) {
    setFormEnabled(!disabled);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  @Override
  public void back() {
    if (this.getSupportFragmentManager().getBackStackEntryCount() == 0) {
      super.back();
    }
  }
}
