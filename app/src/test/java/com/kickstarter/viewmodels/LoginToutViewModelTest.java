package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;

import org.junit.Test;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public class LoginToutViewModelTest extends KSRobolectricTestCase {
  private LoginToutViewModel.ViewModel vm;
  private final TestSubscriber<Void> startLoginActivity = new TestSubscriber<>();
  private final TestSubscriber<Void> startSignupActivity = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new LoginToutViewModel.ViewModel(environment);
    this.vm.outputs.startSignupActivity().subscribe(startSignupActivity);
    this.vm.outputs.startLoginActivity().subscribe(startLoginActivity);
  }

  @Test
  public void testLoginButtonClicked() {
    setUpEnvironment(environment());

    this.startLoginActivity.assertNoValues();

    this.vm.inputs.loginClick();
    this.startLoginActivity.assertValueCount(1);
  }

  @Test
  public void testSignupButtonClicked() {
    setUpEnvironment(environment());

    this.startSignupActivity.assertNoValues();

    this.vm.inputs.signupClick();
    this.startSignupActivity.assertValueCount(1);
  }

  @Test
  public void facebookLoginClick_success() {
    setUpEnvironment(environment());

   this.vm.facebookAccessToken.onNext("token");
  }
}
