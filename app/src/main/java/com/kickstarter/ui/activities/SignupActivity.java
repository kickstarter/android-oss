package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
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
  @Bind(R.id.disclaimer) TextView disclaimerTextView;
  @Bind(R.id.more_button) TextView moreButton; // TODO: move this to a login toolbar

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.signup_layout);
    ButterKnife.bind(this);

    newsletterSwitch.setChecked(true); // TODO: get initial state from presenter
  }

  @Override
  @OnClick(R.id.nav_back_button)
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @OnClick({R.id.more_button, R.id.disclaimer})
  public void moreButtonOnClick() {
   final PopupMenu popup = new PopupMenu(this, moreButton); // TODO: this should be white background and above moreButton
    popup.getMenuInflater().inflate(R.menu.login_help_menu, popup.getMenu());
    popup.setOnMenuItemClickListener(item -> {
      final Intent intent = new Intent(this, HelpActivity.class);
      switch (item.getItemId()) {
        case R.id.terms:
          intent.putExtra(getString(R.string.intent_help_type), HelpActivity.HELP_TYPE_TERMS);
          startActivity(intent);
          overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          break;
        case R.id.privacy_policy:
          intent.putExtra(getString(R.string.intent_help_type), HelpActivity.HELP_TYPE_PRIVACY);
          startActivity(intent);
          overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          break;
        case R.id.cookie_policy:
          intent.putExtra(getString(R.string.intent_help_type), HelpActivity.HELP_TYPE_COOKIE_POLICY);
          startActivity(intent);
          overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          break;
        case R.id.help:
          intent.putExtra(getString(R.string.intent_help_type), HelpActivity.HELP_TYPE_HOW_IT_WORKS);
          startActivity(intent);
          overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
          break;
      }
      return true;
    });
    popup.show();
  }

  @OnTextChanged(R.id.full_name)
  void onNameTextChanged(@NonNull final CharSequence fullName) { }

  @OnTextChanged(R.id.email)
  void onEmailTextChanged(@NonNull final CharSequence email) { }

  @OnTextChanged(R.id.password)
  void onPasswordTextChange(@NonNull final CharSequence password) { }

  @OnCheckedChanged(R.id.newsletter_switch)
  void onNewsletterCheckedChange(@NonNull final CompoundButton newsletterSwitch) {
    //Log.d("gina", "is checked = " + newsletterSwitch.isChecked());
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
