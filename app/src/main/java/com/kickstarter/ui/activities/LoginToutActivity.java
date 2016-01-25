package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.viewmodels.LoginToutViewModel;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(LoginToutViewModel.class)
public final class LoginToutActivity extends BaseActivity<LoginToutViewModel> {
  @Bind(R.id.disclaimer_text_view) TextView disclaimerTextView;
  @Bind(R.id.login_button) Button loginButton;
  @Bind(R.id.facebook_login_button) Button facebookButton;
  @Bind(R.id.sign_up_button) Button signupButton;
  @Bind(R.id.help_button) TextView helpButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;

  @BindString(R.string.login_tout_navbar_title) String loginOrSignUpString;
  @BindString(R.string.login_errors_unable_to_log_in) String unableToLoginString;
  @BindString(R.string.general_error_oops) String errorTitleString;
  @BindString(R.string.login_tout_errors_facebook_authorization_exception_message) String troubleLoggingInString;
  @BindString(R.string.login_tout_errors_facebook_authorization_exception_button) String tryAgainString;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_tout_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(loginOrSignUpString);

    viewModel.errors.facebookAuthorizationError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showDialog(this, errorTitleString, troubleLoggingInString, tryAgainString));

    viewModel.errors.confirmFacebookSignupError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startFacebookConfirmationActivity);

    viewModel.errors.tfaChallenge()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startTwoFactorActivity(true));

    errorMessages()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.showToast(this));

    viewModel.outputs.returnResultAfterLoginSuccess()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> returnResultAfterLoginSuccess());

    viewModel.outputs.startDiscoveryAfterLoginSuccess()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startDiscoveryAfterAfterLoginSuccess());
  }

  private Observable<String> errorMessages() {
    return viewModel.errors.missingFacebookEmailError()
      .map(ObjectUtils.coalesceWith(unableToLoginString))
      .mergeWith(
        viewModel.errors.facebookInvalidAccessTokenError()
          .map(ObjectUtils.coalesceWith(unableToLoginString))
      );
  }

  @OnClick({R.id.disclaimer_text_view})
  public void disclaimerTextViewClick() {
    new LoginPopupMenu(this, helpButton).show();
  }

  @OnClick(R.id.facebook_login_button)
  public void facebookLoginClick() {
    viewModel.inputs.facebookLoginClick(this,
      Arrays.asList(getResources().getStringArray(R.array.facebook_permissions_array))
    );
  }

  @OnClick(R.id.login_button)
  public void loginButtonClick() {
    final Intent intent = new Intent(this, LoginActivity.class);
    if (loginReason.isDefault()) {
      startActivity(intent);
    } else {
      intent.putExtra(IntentKey.FORWARD, true);
      startActivityForResult(intent,
        ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.sign_up_button)
  public void signupButtonClick() {
    final Intent intent = new Intent(this, SignupActivity.class);
    if (loginReason.isDefault()) {
      startActivity(intent);
    } else {
      intent.putExtra(IntentKey.FORWARD, true);
      startActivityForResult(intent,
        ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    viewModel.inputs.activityResult(requestCode, resultCode, intent);

    if (requestCode != ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD) {
      return;
    }

    setResult(resultCode, intent);
    finish();
  }

  public void startFacebookConfirmationActivity(@NonNull final ErrorEnvelope.FacebookUser facebookUser) {
    final Intent intent = new Intent(this, FacebookConfirmationActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, loginReason)
      .putExtra(IntentKey.FACEBOOK_USER, facebookUser)
      .putExtra(IntentKey.FACEBOOK_TOKEN, AccessToken.getCurrentAccessToken().getToken());

    if (loginReason.isDefault()) {
      startActivity(intent);
    } else {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_FACEBOOK_CONFIRMATION_ACTIVITY_FORWARD);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startTwoFactorActivity(final boolean isFacebookLogin) {
    final Intent intent = new Intent(this, TwoFactorActivity.class)
      .putExtra(IntentKey.FACEBOOK_LOGIN, isFacebookLogin)
      .putExtra(IntentKey.FACEBOOK_TOKEN, AccessToken.getCurrentAccessToken().getToken())
      .putExtra(IntentKey.LOGIN_REASON, loginReason);

    if (loginReason.isDefault()) {
      startActivity(intent);
    } else {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startDiscoveryAfterAfterLoginSuccess() {
    final Intent intent = new Intent(this, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }

  private void returnResultAfterLoginSuccess() {
    setResult(Activity.RESULT_OK);
    finish();
  }
}
