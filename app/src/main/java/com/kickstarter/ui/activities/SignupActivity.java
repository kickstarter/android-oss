package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.presenters.SignupPresenter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

@RequiresPresenter(SignupPresenter.class)
public class SignupActivity extends BaseActivity<SignupPresenter> {
  @Bind(R.id.full_name) EditText nameEditText;
  @Bind(R.id.email) EditText emailEditText;
  @Bind(R.id.password) EditText passwordEditText;
  @Bind(R.id.signup_button) Button signupButton;
  @Bind(R.id.newsletter_switch) Switch newsletterSwitch;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.signup_layout);
    ButterKnife.bind(this);

    newsletterSwitch.setChecked(true); //get initial state from presenter
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @OnTextChanged(R.id.full_name)
  void onNameTextChanged(@NonNull final CharSequence fullName) { }

  @OnTextChanged(R.id.email)
  void onEmailTextChanged(@NonNull final CharSequence email) { }

  @OnTextChanged(R.id.password)
  void onPasswordTextChange(@NonNull final CharSequence password) { }

  @OnCheckedChanged(R.id.newsletter_switch)
  void onNewsletterCheckedChange(@NonNull final CompoundButton newsletterSwitch) {
    Log.d("gina", "is checked = " + newsletterSwitch.isChecked());
  }

  @OnClick(R.id.signup_button)
  public void signupButtonOnClick(@NonNull final View view) { }

  public void onSuccess(final boolean forward) {

  }

  public void setFormEnabled(final boolean enabled) { signupButton.setEnabled(enabled); }

  private void displayError(String message, boolean forward) {
    final Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
    toast.show();
  }
}
