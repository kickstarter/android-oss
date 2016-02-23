package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ApiExceptionFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

public final class ResetPasswordViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testResetPasswordViewModel_formValidation() {
    final ResetPasswordViewModel vm = new ResetPasswordViewModel(environment());
    final TestSubscriber<Boolean> test = new TestSubscriber<>();

    koalaTest.assertValues("Forgot Password View");

    vm.outputs.isFormValid().subscribe(test);

    vm.inputs.email("incorrect@kickstarter");
    test.assertValues(false);

    vm.inputs.email("hello@kickstarter.com");
    test.assertValues(false, true);

    koalaTest.assertValueCount(1);
  }

  @Test
  public void testResetPasswordViewModel_resetSuccess() {
    final ResetPasswordViewModel vm = new ResetPasswordViewModel(environment());
    final TestSubscriber<Void> test = new TestSubscriber<>();

    koalaTest.assertValues("Forgot Password View");

    vm.outputs.resetSuccess().subscribe(test);

    vm.inputs.resetPasswordClick();
    test.assertNoValues();

    vm.inputs.email("hello@kickstarter.com");
    test.assertNoValues();

    vm.inputs.resetPasswordClick();
    test.assertValueCount(1);

    koalaTest.assertValues("Forgot Password View", "Forgot Password Requested");
  }

  @Test
  public void testResetPasswordViewModel_resetFailure() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<User> resetPassword(final @NonNull String email) {
        return Observable.error(ApiExceptionFactory.badRequestException());
      }
    };

    final Environment environment = environment().toBuilder()
      .apiClient(apiClient)
      .build();

    final ResetPasswordViewModel vm = new ResetPasswordViewModel(environment);
    final TestSubscriber<String> errorTest = new TestSubscriber<>();

    koalaTest.assertValues("Forgot Password View");

    vm.errors.resetError().subscribe(errorTest);

    vm.inputs.email("hello@kickstarter.com");
    vm.inputs.resetPasswordClick();

    errorTest.assertValue("bad request");

    koalaTest.assertValues("Forgot Password View", "Forgot Password Errored");
  }
}
