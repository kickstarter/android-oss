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
  private LoginViewModel.ViewModel vm;
  private final TestSubscriber<String> genericLoginError = new TestSubscriber<>();
  private final TestSubscriber<String> invalidLoginError = new TestSubscriber<>();
  private final TestSubscriber<Boolean> logInButtonIsEnabled = new TestSubscriber<>();
  private final TestSubscriber<Void> loginSuccess = new TestSubscriber<>();
  private final TestSubscriber<String> preFillEmailFromPasswordReset = new TestSubscriber<>();
  private final TestSubscriber<Boolean> showResetPasswordSuccessDialog = new TestSubscriber<>();
  private final TestSubscriber<Void> tfaChallenge = new TestSubscriber<>();

  @Test
  public void testLoginButtonEnabled() {
    this.vm = new LoginViewModel.ViewModel(environment());
    this.vm.outputs.loginButtonIsEnabled().subscribe(this.logInButtonIsEnabled);

    // Button should not be enabled until both a valid email and password are entered.
    this.vm.inputs.email("hello");
    this.logInButtonIsEnabled.assertNoValues();

    this.vm.inputs.email("hello@kickstarter.com");
    this.logInButtonIsEnabled.assertNoValues();

    this.vm.inputs.password("");
    this.logInButtonIsEnabled.assertValues(false);

    this.vm.inputs.password("izzyiscool");
    this.logInButtonIsEnabled.assertValues(false, true);
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
    this.vm = new LoginViewModel.ViewModel(environment);

    this.vm.outputs.loginSuccess().subscribe(this.loginSuccess);
    this.vm.outputs.genericLoginError().subscribe(this.genericLoginError);

    this.vm.inputs.email("incorrect@kickstarter.com");
    this.vm.inputs.password("lisaiscool");

    this.vm.inputs.loginClick();

    this.loginSuccess.assertNoValues();
    this.genericLoginError.assertValueCount(1);
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
    this.vm = new LoginViewModel.ViewModel(environment);

    this.vm.outputs.loginSuccess().subscribe(this.loginSuccess);
    this.vm.outputs.invalidLoginError().subscribe(this.invalidLoginError);

    this.vm.inputs.email("typo@kickstartr.com");
    this.vm.inputs.password("julieiscool");

    this.vm.inputs.loginClick();

    this.loginSuccess.assertNoValues();
    this.invalidLoginError.assertValueCount(1);
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
    this.vm = new LoginViewModel.ViewModel(environment);
    this.vm.outputs.loginSuccess().subscribe(this.loginSuccess);
    this.vm.outputs.tfaChallenge().subscribe(this.tfaChallenge);

    this.vm.inputs.email("hello@kickstarter.com");
    this.vm.inputs.password("androidiscool");

    this.vm.inputs.loginClick();

    this.loginSuccess.assertNoValues();
    this.tfaChallenge.assertValueCount(1);
  }

  @Test
  public void testPrefillEmailAndDialog() {
    final String email = "hello@kickstarter.com";

    // Start the view model with an email to prefill.
    this.vm = new LoginViewModel.ViewModel(environment());
    this.vm.intent(new Intent().putExtra(IntentKey.EMAIL, email));
    this.vm.outputs.prefillEmailFromPasswordReset().subscribe(this.preFillEmailFromPasswordReset);
    this.vm.outputs.showResetPasswordSuccessDialog()
      .map(showAndEmail -> showAndEmail.first)
      .subscribe(this.showResetPasswordSuccessDialog);

    this.preFillEmailFromPasswordReset.assertValue(email);
    this.showResetPasswordSuccessDialog.assertValue(true);

    // Dismiss the confirmation dialog.
    this.vm.inputs.resetPasswordConfirmationDialogDismissed();
    this.showResetPasswordSuccessDialog.assertValues(true, false);

    // Simulate rotating the device, first by sending a new intent (similar to what happens after rotation).
    this.vm.intent(new Intent().putExtra(IntentKey.EMAIL, email));

    // Create new test subscribers – this emulates a new activity subscribing to the vm's outputs.
    final TestSubscriber<String> rotatedPrefillEmailFromPasswordReset = new TestSubscriber<>();
    this.vm.outputs.prefillEmailFromPasswordReset().subscribe(rotatedPrefillEmailFromPasswordReset);
    final TestSubscriber<Boolean> rotatedShowResetPasswordSuccessDialog = new TestSubscriber<>();
    this.vm.outputs.showResetPasswordSuccessDialog()
      .map(showAndEmail -> showAndEmail.first)
      .subscribe(rotatedShowResetPasswordSuccessDialog);

    // Email should still be pre-filled.
    rotatedPrefillEmailFromPasswordReset.assertValue(email);

    // Dialog should not be shown again – the user has already dismissed it.
    rotatedShowResetPasswordSuccessDialog.assertValue(false);
  }

  @Test
  public void testSuccessfulLogin() {
    this.vm = new LoginViewModel.ViewModel(environment());
    this.vm.outputs.loginSuccess().subscribe(this.loginSuccess);

    this.vm.inputs.email("hello@kickstarter.com");
    this.vm.inputs.password("codeisawesome");

    this.vm.inputs.loginClick();

    this.loginSuccess.assertValueCount(1);
    this.koalaTest.assertValues("Login");
  }
}
