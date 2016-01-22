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

  public static final String REASON_BACK_PROJECT = "pledge";
  public static final String REASON_GENERIC = "generic";
  public static final String REASON_MESSAGE_CREATOR = "new_message";
  public static final String REASON_STAR_PROJECT = "star";

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

  private boolean forward;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_tout_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(loginOrSignUpString);

    // TODO: refactor intent
    final Intent intent = getIntent();
    forward = intent.getBooleanExtra(IntentKey.FORWARD, false);

    viewModel.inputs.reason(intent.getStringExtra(IntentKey.LOGIN_TYPE));

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

    viewModel.outputs.facebookLoginSuccess()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> onSuccess(forward));
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
    if (forward) {
      intent.putExtra(IntentKey.FORWARD, true);
      startActivityForResult(intent,
        ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.sign_up_button)
  public void signupButtonClick() {
    final Intent intent = new Intent(this, SignupActivity.class);
    if (forward) {
      intent.putExtra(IntentKey.FORWARD, true);
      startActivityForResult(intent,
        ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
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

  public void startFacebookConfirmationActivity(@NonNull final ErrorEnvelope.FacebookUser facebookUser) {
    final Intent intent = new Intent(this, FacebookConfirmationActivity.class)
      .putExtra(IntentKey.FORWARD, forward)
      .putExtra(IntentKey.FACEBOOK_USER, facebookUser)
      .putExtra(IntentKey.FACEBOOK_TOKEN, AccessToken.getCurrentAccessToken().getToken());
    if (forward) {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_FACEBOOK_CONFIRMATION_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startTwoFactorActivity(final boolean isFacebookLogin) {
    final Intent intent = new Intent(this, TwoFactorActivity.class)
      .putExtra(IntentKey.FACEBOOK_LOGIN, isFacebookLogin)
      .putExtra(IntentKey.FORWARD, forward)
      .putExtra(IntentKey.FACEBOOK_TOKEN, AccessToken.getCurrentAccessToken().getToken());
    if (forward) {
      startActivityForResult(intent, ActivityRequestCodes.LOGIN_TOUT_ACTIVITY_LOGIN_ACTIVITY_FORWARD);
    } else {
      startActivity(intent);
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
