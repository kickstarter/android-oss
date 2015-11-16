package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.login.LoginManager;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.presenters.TwoFactorPresenter;
import com.kickstarter.ui.toolbars.LoginToolbar;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@RequiresPresenter(TwoFactorPresenter.class)
public final class TwoFactorActivity extends BaseActivity<TwoFactorPresenter> {
  public @Bind(R.id.code) EditText codeEditText;
  public @Bind(R.id.resend_button) Button resendButton;
  public @Bind(R.id.login_button) Button loginButton;
  public @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;

  @BindString(R.string.The_code_provided_does_not_match) String codeMismatchString;
  @BindString(R.string.Unable_to_login) String unableToLoginString;
  @BindString(R.string.Verify) String verifyString;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.two_factor_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(verifyString);

    presenter.inputs.email(getIntent().getExtras().getString(getString(R.string.intent_email)));
    presenter.inputs.isFacebookLogin(getIntent().getBooleanExtra(getString(R.string.intent_facebook_login), false));
    presenter.inputs.fbAccessToken(getIntent().getExtras().getString(getString(R.string.intent_facebook_token)));
    presenter.inputs.password(getIntent().getExtras().getString(getString(R.string.intent_password)));
    final boolean forward = getIntent().getBooleanExtra(getString(R.string.intent_forward), false);

    addSubscription(
      presenter.outputs.tfaSuccess()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(__ -> onSuccess(forward))
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
      errorMessages()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::displayToast)
    );
  }

  private Observable<String> errorMessages() {
    return presenter.errors.tfaCodeMismatchError().map(ObjectUtils.coalesceWith(codeMismatchString))
      .mergeWith(presenter.errors.genericTfaError().map(__ -> unableToLoginString));
  }

  @OnTextChanged(R.id.code)
  public void codeEditTextOnTextChanged(@NonNull final CharSequence code) {
    presenter.inputs.code(code.toString());
  }

  @OnClick(R.id.resend_button)
  public void resendButtonOnClick() {
    presenter.inputs.resendClick();
  }

  @OnClick(R.id.login_button)
  public void loginButtonOnClick() {
    presenter.inputs.loginClick();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);

    // Clear the Facebook user session since TFA was not completed.
    LoginManager.getInstance().logOut();
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

  public void setFormDisabled(final boolean disabled) {
    setFormEnabled(!disabled);
  }
}
