package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ApiExceptionFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

public class LoginViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testLoginButtonEnabled() {
    final LoginViewModel vm = new LoginViewModel(environment());

    final TestSubscriber<Boolean> setLoginButtonIsEnabled = new TestSubscriber<>();
    vm.outputs.setLoginButtonIsEnabled().subscribe(setLoginButtonIsEnabled);

    // Button should not be enabled until both a valid email and password are entered.
    vm.inputs.email("hello");
    setLoginButtonIsEnabled.assertNoValues();

    vm.inputs.email("hello@kickstarter.com");
    setLoginButtonIsEnabled.assertNoValues();

    vm.inputs.password("");
    setLoginButtonIsEnabled.assertValues(false);

    vm.inputs.password("danisawesome");
    setLoginButtonIsEnabled.assertValues(false, true);
  }

  @Test
  public void testLoginApiError() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password) {
        return Observable.error(ApiExceptionFactory.badRequestException());
      }
    };

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();
    final LoginViewModel vm = new LoginViewModel(environment);

    final TestSubscriber<Void> loginSuccess = new TestSubscriber<>();
    vm.outputs.loginSuccess().subscribe(loginSuccess);
    
    final TestSubscriber<String> genericLoginError = new TestSubscriber<>();
    vm.errors.genericLoginError().subscribe(genericLoginError);

    vm.inputs.email("incorrect@kickstarter.com");
    vm.inputs.password("danisawesome");

    vm.inputs.loginClick();

    loginSuccess.assertNoValues();
    genericLoginError.assertValueCount(1);
  }

  @Test
  public void testLoginApiValidationError() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password) {
        return Observable.error(ApiExceptionFactory.invalidLoginException());
      }
    };

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();
    final LoginViewModel vm = new LoginViewModel(environment);

    final TestSubscriber<String> invalidLoginError = new TestSubscriber<>();
    vm.errors.invalidLoginError().subscribe(invalidLoginError);

    final TestSubscriber<Void> loginSuccess = new TestSubscriber<>();
    vm.outputs.loginSuccess().subscribe(loginSuccess);

    vm.inputs.email("typo@kickstartr.com");
    vm.inputs.password("danisawesome");

    vm.inputs.loginClick();

    loginSuccess.assertNoValues();
    invalidLoginError.assertValueCount(1);
  }

  @Test
  public void testLoginTfaChallenge() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password) {
        return Observable.error(ApiExceptionFactory.tfaRequired());
      }
    };

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();
    final LoginViewModel vm = new LoginViewModel(environment);

    final TestSubscriber<Void> tfaChallenge = new TestSubscriber<>();
    vm.errors.tfaChallenge().subscribe(tfaChallenge);

    final TestSubscriber<Void> loginSuccess = new TestSubscriber<>();
    vm.outputs.loginSuccess().subscribe(loginSuccess);

    vm.inputs.email("hello@kickstarter.com");
    vm.inputs.password("danisawesome");

    vm.inputs.loginClick();

    loginSuccess.assertNoValues();
    tfaChallenge.assertValueCount(1);
  }

  @Test
  public void testPrefillEmailAndDialog() {
    final String email = "hello@kickstarter.com";

    final LoginViewModel vm = new LoginViewModel(environment());

    final TestSubscriber<String> prefillEmailFromPasswordReset = new TestSubscriber<>();
    vm.outputs.prefillEmailFromPasswordReset().subscribe(prefillEmailFromPasswordReset);

    final TestSubscriber<Boolean> showResetPasswordSuccessDialog = new TestSubscriber<>();
    vm.outputs.showResetPasswordSuccessDialog()
      .map(showAndEmail -> showAndEmail.first)
      .subscribe(showResetPasswordSuccessDialog);

    prefillEmailFromPasswordReset.assertNoValues();
    showResetPasswordSuccessDialog.assertNoValues();

    // Start the view model with an email to prefill.
    vm.intent(new Intent().putExtra(IntentKey.EMAIL, email));

    prefillEmailFromPasswordReset.assertValue(email);
    showResetPasswordSuccessDialog.assertValue(true);

    // Dismiss the confirmation dialog.
    vm.inputs.resetPasswordConfirmationDialogDismissed();

    // Simulate rotating the device.
    final TestSubscriber<String> rotatedPrefillEmailFromPasswordReset = new TestSubscriber<>();
    vm.outputs.prefillEmailFromPasswordReset().subscribe(rotatedPrefillEmailFromPasswordReset);
    final TestSubscriber<Boolean> rotatedShowResetPasswordSuccessDialog = new TestSubscriber<>();
    vm.outputs.showResetPasswordSuccessDialog()
      .map(showAndEmail -> showAndEmail.first)
      .subscribe(rotatedShowResetPasswordSuccessDialog);

    // Email should still be filled. Dialog should not be shown again.
    rotatedPrefillEmailFromPasswordReset.assertValue(email);
    rotatedShowResetPasswordSuccessDialog.assertValue(false);
  }

  @Test
  public void testSuccessfulLogin() {
    final LoginViewModel vm = new LoginViewModel(environment());

    final TestSubscriber<Void> loginSuccess  = new TestSubscriber<>();
    vm.outputs.loginSuccess().subscribe(loginSuccess);

    vm.inputs.email("hello@kickstarter.com");
    vm.inputs.password("danisawesome");

    vm.inputs.loginClick();

    loginSuccess.assertValueCount(1);
    koalaTest.assertValues("Login");
  }
}
