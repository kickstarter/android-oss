package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ApiExceptionFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

public class TwoFactorViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testTwoFactorViewModel_FormValidation() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, false);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "");

    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(intent);

    final TestSubscriber<Boolean> formIsValid = new TestSubscriber<>();
    vm.outputs.formIsValid().subscribe(formIsValid);

    formIsValid.assertNoValues();

    vm.inputs.code("444444");
    formIsValid.assertValue(true);

    vm.inputs.code("");
    formIsValid.assertValues(true, false);

    koalaTest.assertValue("Two-factor Authentication Confirm View");
  }

  @Test
  public void testTwoFactorViewModel_TfaSuccess() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, false);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "");

    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(intent);

    final TestSubscriber<Void> tfaSuccess = new TestSubscriber<>();
    vm.outputs.tfaSuccess().subscribe(tfaSuccess);

    final TestSubscriber<Boolean> formSubmitting = new TestSubscriber<>();
    vm.outputs.formSubmitting().subscribe(formSubmitting);

    vm.inputs.code("88888");
    vm.inputs.loginClick();

    formSubmitting.assertValues(true, false);
    tfaSuccess.assertValueCount(1);

    koalaTest.assertValues("Two-factor Authentication Confirm View", "Login");
  }

  @Test
  public void testTwoFactorViewModel_TfaSuccessFacebook() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, true);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234");

    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(intent);

    final TestSubscriber<Void> tfaSuccess = new TestSubscriber<>();
    vm.outputs.tfaSuccess().subscribe(tfaSuccess);

    final TestSubscriber<Boolean> formSubmitting = new TestSubscriber<>();
    vm.outputs.formSubmitting().subscribe(formSubmitting);

    vm.inputs.code("88888");
    vm.inputs.loginClick();

    formSubmitting.assertValues(true, false);
    tfaSuccess.assertValueCount(1);

    koalaTest.assertValues("Two-factor Authentication Confirm View", "Login");
  }

  @Test
   public void testTwoFactorViewModel_ResendCode() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, false);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "");

    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(intent);

    final TestSubscriber<Void> showResendCodeConfirmation = new TestSubscriber<>();
    vm.outputs.showResendCodeConfirmation().subscribe(showResendCodeConfirmation);

    vm.inputs.resendClick();

    showResendCodeConfirmation.assertValueCount(1);
    koalaTest.assertValues("Two-factor Authentication Confirm View", "Two-factor Authentication Resend Code");
  }

  @Test
  public void testTwoFactorViewModel_ResendCodeFacebook() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, true);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234");

    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(intent);

    final TestSubscriber<Void> showResendCodeConfirmation = new TestSubscriber<>();
    vm.outputs.showResendCodeConfirmation().subscribe(showResendCodeConfirmation);

    vm.inputs.resendClick();

    showResendCodeConfirmation.assertValueCount(1);
    koalaTest.assertValues("Two-factor Authentication Confirm View", "Two-factor Authentication Resend Code");
  }

  @Test
  public void testTwoFactorViewModel_GenericError() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull
        Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password,
        final @NonNull String code) {
        return Observable.error(ApiExceptionFactory.apiError(
          ErrorEnvelope.builder().httpCode(400).build()
        ));
      }
    };

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();

    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, false);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "");

    final TwoFactorViewModel vm = new TwoFactorViewModel(environment);
    vm.intent(intent);

    final TestSubscriber<Void> tfaSuccess = new TestSubscriber<>();
    vm.outputs.tfaSuccess().subscribe(tfaSuccess);

    final TestSubscriber<Boolean> formSubmitting = new TestSubscriber<>();
    vm.outputs.formSubmitting().subscribe(formSubmitting);

    final TestSubscriber<Void> genericTfaError = new TestSubscriber<>();
    vm.errors.genericTfaError().subscribe(genericTfaError);

    vm.inputs.code("88888");
    vm.inputs.loginClick();

    formSubmitting.assertValues(true, false);
    tfaSuccess.assertNoValues();
    genericTfaError.assertValueCount(1);
    koalaTest.assertValues("Two-factor Authentication Confirm View", "Errored User Login");
  }

  @Test
  public void testTwoFactorViewModel_CodeMismatchError() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull
        Observable<AccessTokenEnvelope> login(final @NonNull String email, final @NonNull String password,
        final @NonNull String code) {
        return Observable.error(ApiExceptionFactory.tfaFailed());
      }
    };

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();

    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, false);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "");

    final TwoFactorViewModel vm = new TwoFactorViewModel(environment);
    vm.intent(intent);

    final TestSubscriber<Void> tfaSuccess = new TestSubscriber<>();
    vm.outputs.tfaSuccess().subscribe(tfaSuccess);

    final TestSubscriber<Boolean> formSubmitting = new TestSubscriber<>();
    vm.outputs.formSubmitting().subscribe(formSubmitting);

    final TestSubscriber<Void> tfaCodeMismatchError = new TestSubscriber<>();
    vm.errors.tfaCodeMismatchError().subscribe(tfaCodeMismatchError);

    vm.inputs.code("88888");
    vm.inputs.loginClick();

    formSubmitting.assertValues(true, false);
    tfaSuccess.assertNoValues();
    tfaCodeMismatchError.assertValueCount(1);
    koalaTest.assertValues("Two-factor Authentication Confirm View", "Errored User Login");
  }
}
