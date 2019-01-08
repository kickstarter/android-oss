package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.LoginToolbar;
import com.kickstarter.ui.views.LoginPopupMenu;
import com.kickstarter.viewmodels.LoginToutViewModel;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromRight;
import static com.kickstarter.libs.utils.TransitionUtils.transition;

@RequiresActivityViewModel(LoginToutViewModel.ViewModel.class)
public final class LoginToutActivity extends BaseActivity<LoginToutViewModel.ViewModel> {
  @Bind(R.id.disclaimer_text_view) TextView disclaimerTextView;
  @Bind(R.id.login_button) Button loginButton;
  @Bind(R.id.facebook_login_button) Button facebookButton;
  @Bind(R.id.sign_up_button) Button signupButton;
  @Bind(R.id.help_button) TextView helpButton;
  @Bind(R.id.login_toolbar) LoginToolbar loginToolbar;

  @BindString(R.string.login_tout_navbar_title) String loginOrSignUpString;
  @BindString(R.string.login_errors_title) String loginErrorTitleString;
  @BindString(R.string.login_errors_unable_to_log_in) String unableToLoginString;
  @BindString(R.string.general_error_oops) String errorTitleString;
  @BindString(R.string.login_tout_errors_facebook_authorization_exception_message) String troubleLoggingInString;
  @BindString(R.string.login_tout_errors_facebook_authorization_exception_button) String tryAgainString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login_tout_layout);
    ButterKnife.bind(this);
    this.loginToolbar.setTitle(this.loginOrSignUpString);

    this.viewModel.outputs.finishWithSuccessfulResult()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> finishWithSuccessfulResult());

    this.viewModel.outputs.startLoginActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startLogin());

    this.viewModel.outputs.startSignupActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startSignup());

    this.viewModel.outputs.startFacebookConfirmationActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ua -> startFacebookConfirmationActivity(ua.first, ua.second));

    this.viewModel.outputs.showFacebookAuthorizationErrorDialog()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showDialog(this, this.errorTitleString, this.troubleLoggingInString, this.tryAgainString));

    showErrorMessageToasts()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.showToast(this));

    this.viewModel.outputs.startTwoFactorChallenge()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startTwoFactorFacebookChallenge());

    this.viewModel.outputs.showUnauthorizedErrorDialog()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(errorMessage -> ViewUtils.showDialog(this, this.loginErrorTitleString, errorMessage));
  }

  private @NonNull Observable<String> showErrorMessageToasts() {
    return this.viewModel.outputs.showMissingFacebookEmailErrorToast()
      .map(ObjectUtils.coalesceWith(this.unableToLoginString))
      .mergeWith(
        this.viewModel.outputs.showFacebookInvalidAccessTokenErrorToast()
          .map(ObjectUtils.coalesceWith(this.unableToLoginString))
      );
  }

  @OnClick(R.id.disclaimer_text_view)
  public void disclaimerTextViewClick() {
    new LoginPopupMenu(this, this.helpButton).show();
  }

  @OnClick(R.id.facebook_login_button)
  public void facebookLoginClick() {
    this.viewModel.inputs.facebookLoginClick(this,
      Arrays.asList(getResources().getStringArray(R.array.facebook_permissions_array))
    );
  }

  @OnClick(R.id.login_button)
  public void loginButtonClick() {
    this.viewModel.inputs.loginClick();
  }

  @OnClick(R.id.sign_up_button)
  public void signupButtonClick() {
    this.viewModel.inputs.signupClick();
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
