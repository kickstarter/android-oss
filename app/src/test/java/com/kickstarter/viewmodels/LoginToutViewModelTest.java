package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.User;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.data.LoginReason;

import org.junit.Test;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.observers.TestSubscriber;

public class LoginToutViewModelTest extends KSRobolectricTestCase {
  private LoginToutViewModel.ViewModel vm;
  private final TestSubscriber<Void> finishWithSuccessfulResult = new TestSubscriber<>();
  private final TestSubscriber<ErrorEnvelope> loginError = new TestSubscriber<>();
  private final TestSubscriber<Void> startLoginActivity = new TestSubscriber<>();
  private final TestSubscriber<Void> startSignupActivity = new TestSubscriber<>();
  private final TestSubscriber<User> currentUser = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment, final @NonNull LoginReason loginReason) {
    this.vm = new LoginToutViewModel.ViewModel(environment);

    this.vm.outputs.finishWithSuccessfulResult().subscribe(this.finishWithSuccessfulResult);
    this.vm.loginError.subscribe(this.loginError);
    this.vm.outputs.startSignupActivity().subscribe(this.startSignupActivity);
    this.vm.outputs.startLoginActivity().subscribe(this.startLoginActivity);
    environment.currentUser().observable().subscribe(this.currentUser);

    this.vm.intent(new Intent().putExtra(IntentKey.LOGIN_REASON, loginReason));
  }

  @Test
  public void testLoginButtonClicked() {
    setUpEnvironment(environment(), LoginReason.DEFAULT);

    this.startLoginActivity.assertNoValues();

    this.vm.inputs.loginClick();
    this.startLoginActivity.assertValueCount(1);
    this.lakeTest.assertValue("Log In or Sign Up Page Viewed");
  }

  @Test
  public void testSignupButtonClicked() {
    setUpEnvironment(environment(), LoginReason.DEFAULT);

    this.startSignupActivity.assertNoValues();

    this.vm.inputs.signupClick();
    this.startSignupActivity.assertValueCount(1);
    this.lakeTest.assertValue("Log In or Sign Up Page Viewed");
  }

  @Test
  public void facebookLogin_success() {
    final MockCurrentUser currentUser = new MockCurrentUser();
    final Environment environment = environment()
      .toBuilder()
      .currentUser(currentUser)
      .build();
    setUpEnvironment(environment, LoginReason.DEFAULT);

    this.currentUser.assertValuesAndClear(null);
    this.vm.facebookAccessToken.onNext("token");
    this.currentUser.assertValueCount(1);
    this.finishWithSuccessfulResult.assertValueCount(1);
    this.lakeTest.assertValue("Log In or Sign Up Page Viewed");
  }

  @Test
  public void facebookLogin_error() {
    final MockCurrentUser currentUser = new MockCurrentUser();
    final Environment environment = environment()
      .toBuilder()
      .currentUser(currentUser)
      .apiClient(new MockApiClient() {
        @Override
        public @NonNull Observable<AccessTokenEnvelope> loginWithFacebook(final @NonNull String accessToken) {
          return Observable.error(new Throwable("error"));
        }
      })
      .build();

    setUpEnvironment(environment, LoginReason.DEFAULT);

    this.currentUser.assertValuesAndClear(null);
    this.vm.facebookAccessToken.onNext("token");
    this.currentUser.assertNoValues();
    this.finishWithSuccessfulResult.assertNoValues();
    this.lakeTest.assertValue("Log In or Sign Up Page Viewed");
  }
}
