package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.presenters.LoginPresenter;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;

@RequiresPresenter(LoginPresenter.class)
public class LoginActivity extends BaseActivity<LoginPresenter> {
  @Bind(R.id.email) EditText emailEditText;
  @Bind(R.id.login_button) Button loginButton;
  @Bind(R.id.password) EditText passwordEditText;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_layout);
    ButterKnife.bind(this);

    final boolean forward = getIntent().getBooleanExtra(getString(R.string.intent_forward), false);

    final Observable<String> errorMessage = presenter.errors().invalidLoginError().map(__ -> R.string.Login_does_not_match_any_of_our_records)
      .mergeWith(presenter.errors().genericLoginError().map(__ -> R.string.Unable_to_login))
      .map(this::getString);

    addSubscription(
      errorMessage.subscribe(message -> {
        this.displayError(message, forward);
      })
    );

    addSubscription(
      presenter.errors().tfaChallenge().subscribe(__ -> {
        startTwoFactorActivity(forward);
      })
    );

    addSubscription(
      presenter.outputs().loginSuccess().subscribe(__ -> {
        onSuccess(forward);
      })
    );
  }

  private void displayError(String message, boolean forward) {
    final Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
    toast.show();
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
    presenter.inputs().email(email.toString());
  }

  @OnTextChanged(R.id.password)
  void onPasswordTextChanged(@NonNull final CharSequence password) {
    presenter.inputs().password(password.toString());
  }

  @OnClick(R.id.login_button)
  public void loginButtonOnClick(@NonNull final View view) {
    presenter.inputs().loginClick(view);
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
      .putExtra(getString(R.string.intent_email), emailEditText.getText().toString())
      .putExtra(getString(R.string.intent_password), passwordEditText.getText().toString())
      .putExtra(getString(R.string.intent_forward), forward);
    if (forward) {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_ACTIVITY_TWO_FACTOR_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
  }
}
