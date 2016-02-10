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
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;
import com.kickstarter.viewmodels.LoginToutViewModel;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromRight;
import static com.kickstarter.libs.utils.TransitionUtils.transition;

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
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_tout_layout);
    ButterKnife.bind(this);
    loginToolbar.setTitle(loginOrSignUpString);

    viewModel.outputs.finishWithSuccessfulResult()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> finishWithSuccessfulResult());

    viewModel.outputs.startLogin()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startLogin());

    viewModel.outputs.startSignup()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startSignup());

    viewModel.outputs.startConfirmFacebookSignup()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ua -> startFacebookConfirmationActivity(ua.first, ua.second));

    viewModel.errors.facebookAuthorizationError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showDialog(this, errorTitleString, troubleLoggingInString, tryAgainString));

    errorMessages()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.showToast(this));

    viewModel.errors.startTwoFactorChallenge()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startTwoFactorFacebookChallenge());
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
    viewModel.inputs.loginClick();
  }

  @OnClick(R.id.sign_up_button)
  public void signupButtonClick() {
    viewModel.inputs.signupClick();
  }

  private void finishWithSuccessfulResult() {
    setResult(Activity.RESULT_OK);
    finish();
  }

  public void startFacebookConfirmationActivity(final @NonNull ErrorEnvelope.FacebookUser facebookUser,
    final @NonNull String accessTokenString) {
    final Intent intent = new Intent(this, FacebookConfirmationActivity.class)
      .putExtra(IntentKey.FACEBOOK_USER, facebookUser)
      .putExtra(IntentKey.FACEBOOK_TOKEN, accessTokenString);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    transition(this, slideInFromRight());
  }

  private void startLogin() {
    final Intent intent = new Intent(this, LoginActivity.class);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    transition(this, slideInFromRight());
  }

  private void startSignup() {
    final Intent intent = new Intent(this, SignupActivity.class);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    transition(this, slideInFromRight());
  }

  public void startTwoFactorFacebookChallenge() {
    final Intent intent = new Intent(this, TwoFactorActivity.class)
      .putExtra(IntentKey.FACEBOOK_LOGIN, true)
      .putExtra(IntentKey.FACEBOOK_TOKEN, AccessToken.getCurrentAccessToken().getToken());

    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
    transition(this, slideInFromRight());
  }
}
