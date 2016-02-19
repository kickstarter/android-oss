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

    vm.outputs.isFormValid().subscribe(test);
    test.assertNoValues();

    vm.inputs.email("incorrect@kickstarter");
    test.assertValues(false);

    vm.inputs.email("hello@kickstarter.com");
    test.assertValues(false, true);
  }

  @Test
  public void testResetPasswordViewModel_resetSuccess() {
    final ResetPasswordViewModel vm = new ResetPasswordViewModel(environment());
    final TestSubscriber<Void> test = new TestSubscriber<>();

    vm.outputs.resetSuccess().subscribe(test);
    test.assertNoValues();

    vm.inputs.resetPasswordClick();
    test.assertNoValues();

    vm.inputs.email("hello@kickstarter.com");
    test.assertNoValues();

    vm.inputs.resetPasswordClick();
    test.assertValueCount(1);
  }

  @Test
  public void testResetPasswordViewModel_resetFailure() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<User> resetPassword(final @NonNull String email) {
        return Observable.error(ApiExceptionFactory.badRequestException());
      }
    };
    final Environment environment = environment().toBuilder().apiClient(apiClient).build();

    final ResetPasswordViewModel vm = new ResetPasswordViewModel(environment);
    final TestSubscriber<String> test = new TestSubscriber<>();

    vm.errors.resetError().subscribe(test);
    test.assertNoValues();

    vm.inputs.email("hello@kickstarter.com");
    vm.inputs.resetPasswordClick();

    test.assertValue("bad request");
  }
}
