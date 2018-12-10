package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ApiExceptionFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

public class TwoFactorViewModelTest extends KSRobolectricTestCase {
  private TwoFactorViewModel.ViewModel vm;
  private final TestSubscriber<Boolean> formIsValid = new TestSubscriber<>();
  private final TestSubscriber<Boolean> formSubmitting = new TestSubscriber<>();
  private final TestSubscriber<Void> genericTfaError = new TestSubscriber<>();
  private final TestSubscriber<Void> showResendCodeConfirmation = new TestSubscriber<>();
  private final TestSubscriber<Void> tfaCodeMismatchError = new TestSubscriber<>();
  private final TestSubscriber<Void> tfaSuccess = new TestSubscriber<>();

  @Test
  public void testTwoFactorViewModel_FormValidation() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, false);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "");

    this.vm = new TwoFactorViewModel.ViewModel(environment());
    this.vm.intent(intent);

    this.vm.outputs.formIsValid().subscribe(this.formIsValid);

    this.formIsValid.assertNoValues();

    this.vm.inputs.code("444444");
    this.formIsValid.assertValue(true);

    this.vm.inputs.code("");
    this.formIsValid.assertValues(true, false);

    this.koalaTest.assertValue(KoalaEvent.TWO_FACTOR_AUTH_CONFIRM_VIEW);
  }

  @Test
  public void testTwoFactorViewModel_TfaSuccess() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, false);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "");

    this.vm = new TwoFactorViewModel.ViewModel(environment());
    this.vm.intent(intent);

    this.vm.outputs.tfaSuccess().subscribe(this.tfaSuccess);
    this.vm.outputs.formSubmitting().subscribe(this.formSubmitting);

    this.vm.inputs.code("88888");
    this.vm.inputs.loginClick();

    this.formSubmitting.assertValues(true, false);
    this.tfaSuccess.assertValueCount(1);

    this.koalaTest.assertValues(KoalaEvent.TWO_FACTOR_AUTH_CONFIRM_VIEW, KoalaEvent.LOGIN);
  }

  @Test
  public void testTwoFactorViewModel_TfaSuccessFacebook() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, true);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234");

    this.vm = new TwoFactorViewModel.ViewModel(environment());
    this.vm.intent(intent);

    this.vm.outputs.tfaSuccess().subscribe(this.tfaSuccess);
    this.vm.outputs.formSubmitting().subscribe(this.formSubmitting);

    this.vm.inputs.code("88888");
    this.vm.inputs.loginClick();

    this.formSubmitting.assertValues(true, false);
    this.tfaSuccess.assertValueCount(1);

    this.koalaTest.assertValues(KoalaEvent.TWO_FACTOR_AUTH_CONFIRM_VIEW, KoalaEvent.LOGIN);
  }

  @Test
   public void testTwoFactorViewModel_ResendCode() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, false);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "");

    this.vm = new TwoFactorViewModel.ViewModel(environment());
    this.vm.intent(intent);

    this.vm.outputs.showResendCodeConfirmation().subscribe(this.showResendCodeConfirmation);

    this.vm.inputs.resendClick();

    this.showResendCodeConfirmation.assertValueCount(1);
    this.koalaTest.assertValues(KoalaEvent.TWO_FACTOR_AUTH_CONFIRM_VIEW, KoalaEvent.TWO_FACTOR_AUTH_RESEND_CODE);
  }

  @Test
  public void testTwoFactorViewModel_ResendCodeFacebook() {
    final Intent intent = new Intent();
    intent.putExtra(IntentKey.EMAIL, "gina@kickstarter.com");
    intent.putExtra(IntentKey.PASSWORD, "hello");
    intent.putExtra(IntentKey.FACEBOOK_LOGIN, true);
    intent.putExtra(IntentKey.FACEBOOK_TOKEN, "pajamas1234");

    this.vm = new TwoFactorViewModel.ViewModel(environment());
    this.vm.intent(intent);

    this.vm.outputs.showResendCodeConfirmation().subscribe(this.showResendCodeConfirmation);

    this.vm.inputs.resendClick();

    this.showResendCodeConfirmation.assertValueCount(1);
    this.koalaTest.assertValues(KoalaEvent.TWO_FACTOR_AUTH_CONFIRM_VIEW, KoalaEvent.TWO_FACTOR_AUTH_RESEND_CODE);
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

    this.vm = new TwoFactorViewModel.ViewModel(environment);
    this.vm.intent(intent);

    this.vm.outputs.tfaSuccess().subscribe(this.tfaSuccess);
    this.vm.outputs.formSubmitting().subscribe(this.formSubmitting);
    this.vm.outputs.genericTfaError().subscribe(this.genericTfaError);

    this.vm.inputs.code("88888");
    this.vm.inputs.loginClick();

    this.formSubmitting.assertValues(true, false);
    this.tfaSuccess.assertNoValues();
    this.genericTfaError.assertValueCount(1);
    this.koalaTest.assertValues(KoalaEvent.TWO_FACTOR_AUTH_CONFIRM_VIEW, KoalaEvent.ERRORED_USER_LOGIN);
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

    this.vm = new TwoFactorViewModel.ViewModel(environment);
    this.vm.intent(intent);

    this.vm.outputs.tfaSuccess().subscribe(this.tfaSuccess);
    this.vm.outputs.formSubmitting().subscribe(this.formSubmitting);
    this.vm.outputs.tfaCodeMismatchError().subscribe(this.tfaCodeMismatchError);

    this.vm.inputs.code("88888");
    this.vm.inputs.loginClick();

    this.formSubmitting.assertValues(true, false);
    this.tfaSuccess.assertNoValues();
    this.tfaCodeMismatchError.assertValueCount(1);
    this.koalaTest.assertValues(KoalaEvent.TWO_FACTOR_AUTH_CONFIRM_VIEW, KoalaEvent.ERRORED_USER_LOGIN);
  }
}
