package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class TwoFactorViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testTwoFactorViewModel_FormValidation() {
    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(new Intent().putExtra(IntentKey.EMAIL, "gina@kickstarter.com"));
    vm.intent(new Intent().putExtra(IntentKey.PASSWORD, "hello"));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_LOGIN, false));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234"));

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
    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(new Intent().putExtra(IntentKey.EMAIL, "gina@kickstarter.com"));
    vm.intent(new Intent().putExtra(IntentKey.PASSWORD, "hello"));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_LOGIN, false));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234"));
    vm.inputs.code("88888");

    final TestSubscriber<Void> tfaSuccess = new TestSubscriber<>();
    vm.outputs.tfaSuccess().subscribe(tfaSuccess);

    final TestSubscriber<Boolean> formSubmitting = new TestSubscriber<>();
    vm.outputs.formSubmitting().subscribe(formSubmitting);

    vm.inputs.loginClick();
    formSubmitting.assertValues(true, false);
    tfaSuccess.assertValueCount(1);

    koalaTest.assertValues("Two-factor Authentication Confirm View", "Login");
  }

  @Test
  public void testTwoFactorViewModel_FacebookLoginSuccess() {
    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(new Intent().putExtra(IntentKey.EMAIL, "gina@kickstarter.com"));
    vm.intent(new Intent().putExtra(IntentKey.PASSWORD, "hello"));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_LOGIN, true));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234"));
    vm.inputs.code("88888");

    final TestSubscriber<Void> tfaSuccess = new TestSubscriber<>();
    vm.outputs.tfaSuccess().subscribe(tfaSuccess);

    final TestSubscriber<Boolean> formSubmitting = new TestSubscriber<>();
    vm.outputs.formSubmitting().subscribe(formSubmitting);

    vm.inputs.loginClick();
    formSubmitting.assertValues(true, false);
    tfaSuccess.assertValueCount(1);

    koalaTest.assertValues("Two-factor Authentication Confirm View", "Login");
  }

  @Test
   public void testTwoFactorViewModel_ResendCode() {
    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(new Intent().putExtra(IntentKey.EMAIL, "gina@kickstarter.com"));
    vm.intent(new Intent().putExtra(IntentKey.PASSWORD, "hello"));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_LOGIN, false));

    vm.inputs.resendClick();

    koalaTest.assertValues("Two-factor Authentication Confirm View", "Two-factor Authentication Resend Code");
  }

  @Test
  public void testTwoFactorViewModel_ResendCodeFacebook() {
    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(new Intent().putExtra(IntentKey.EMAIL, "gina@kickstarter.com"));
    vm.intent(new Intent().putExtra(IntentKey.PASSWORD, "hello"));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_LOGIN, true));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234"));

    vm.inputs.resendClick();

    koalaTest.assertValues("Two-factor Authentication Confirm View", "Two-factor Authentication Resend Code");
  }

  @Test
  public void testTwoFactorViewModel_GenericError() {
    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(new Intent().putExtra(IntentKey.EMAIL, "gina@kickstarter.com"));
    vm.intent(new Intent().putExtra(IntentKey.PASSWORD, "hello"));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_LOGIN, false));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234"));
    vm.inputs.code("88888");

    final TestSubscriber<Void> genericTfaError = new TestSubscriber<>();
    vm.errors.genericTfaError().subscribe(genericTfaError);

    //genericTfaError.assertValueCount(1);
    //koalaTest.assertValues("Two-factor Authentication Confirm View", "Errored User Login");
  }

  @Test
  public void testTwoFactorViewModel_CodeMismatchError() {
    final TwoFactorViewModel vm = new TwoFactorViewModel(environment());
    vm.intent(new Intent().putExtra(IntentKey.EMAIL, "gina@kickstarter.com"));
    vm.intent(new Intent().putExtra(IntentKey.PASSWORD, "hello"));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_LOGIN, false));
    vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234"));
    vm.inputs.code("88888");

    final TestSubscriber<String> tfaCodeMismatchError = new TestSubscriber<>();
    vm.errors.tfaCodeMismatchError().subscribe(tfaCodeMismatchError);

    //tfaCodeMismatchError.assertValueCount(1);
    //koalaTest.assertValues("Two-factor Authentication Confirm View", "Errored User Login");
  }
}
