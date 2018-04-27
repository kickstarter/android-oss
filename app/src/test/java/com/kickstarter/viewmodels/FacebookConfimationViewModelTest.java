package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ApiExceptionFactory;
import com.kickstarter.factories.ConfigFactory;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentConfig;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.services.apiresponses.AccessTokenEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import java.util.Collections;

import rx.Observable;
import rx.observers.TestSubscriber;

public class FacebookConfimationViewModelTest extends KSRobolectricTestCase {
  private FacebookConfirmationViewModel.ViewModel vm;
  private final TestSubscriber<String> prefillEmail = new TestSubscriber<>();
  private final TestSubscriber<String> signupError = new TestSubscriber<>();
  private final TestSubscriber<Void> signupSuccess = new TestSubscriber<>();
  private final TestSubscriber<Boolean> sendNewslettersIsChecked = new TestSubscriber<>();

  @Test
  public void testPrefillEmail() {
    final ErrorEnvelope.FacebookUser facebookUser = ErrorEnvelope.FacebookUser.builder()
      .id(1).name("Test").email("test@kickstarter.com")
      .build();

    this.vm = new FacebookConfirmationViewModel.ViewModel(environment());
    this.vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_USER, facebookUser));

    this.vm.outputs.prefillEmail().subscribe(this.prefillEmail);

    this.prefillEmail.assertValue("test@kickstarter.com");
    this.koalaTest.assertValues(KoalaEvent.FACEBOOK_CONFIRM, KoalaEvent.USER_SIGNUP);
  }

  @Test
  public void testSignupErrorDisplay() {
    final ApiClientType apiClient = new MockApiClient() {
      @Override
      public @NonNull Observable<AccessTokenEnvelope> registerWithFacebook(final @NonNull String fbAccessToken, final boolean sendNewsletters) {
        return Observable.error(ApiExceptionFactory.apiError(
          ErrorEnvelope.builder().httpCode(404).errorMessages(Collections.singletonList("oh no")).build())
        );
      }
    };

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();
    this.vm = new FacebookConfirmationViewModel.ViewModel(environment);

    this.vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "token"));
    this.vm.outputs.signupError().subscribe(this.signupError);

    this.koalaTest.assertValues(KoalaEvent.FACEBOOK_CONFIRM, KoalaEvent.USER_SIGNUP);

    this.vm.inputs.sendNewslettersClick(true);
    this.vm.inputs.createNewAccountClick();

    this.signupError.assertValue("oh no");
    this.koalaTest.assertValues(KoalaEvent.FACEBOOK_CONFIRM, KoalaEvent.USER_SIGNUP,
      KoalaEvent.SIGNUP_NEWSLETTER_TOGGLE, KoalaEvent.ERRORED_USER_SIGNUP);
  }

  @Test
  public void testSuccessfulUserCreation() {
    final ApiClientType apiClient = new MockApiClient();

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();
    this.vm = new FacebookConfirmationViewModel.ViewModel(environment);

    this.vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "token"));
    this.vm.outputs.signupSuccess().subscribe(this.signupSuccess);

    this.vm.inputs.sendNewslettersClick(true);
    this.vm.inputs.createNewAccountClick();

    this.signupSuccess.assertValueCount(1);
    this.koalaTest.assertValues(KoalaEvent.FACEBOOK_CONFIRM, KoalaEvent.USER_SIGNUP,
      KoalaEvent.SIGNUP_NEWSLETTER_TOGGLE, KoalaEvent.LOGIN, KoalaEvent.NEW_USER);
  }

  @Test
  public void testToggleSendNewsLetter_isNotChecked() {
    final CurrentConfigType currentConfig = new MockCurrentConfig();
    currentConfig.config(ConfigFactory.config().toBuilder().countryCode("US").build());
    final Environment environment = environment().toBuilder().currentConfig(currentConfig).build();
    this.vm = new FacebookConfirmationViewModel.ViewModel(environment);

    this.vm.outputs.sendNewslettersIsChecked().subscribe(this.sendNewslettersIsChecked);
    this.sendNewslettersIsChecked.assertValue(false);

    this.vm.inputs.sendNewslettersClick(true);
    this.vm.inputs.sendNewslettersClick(false);

    this.sendNewslettersIsChecked.assertValues(false, true, false);
    this.koalaTest.assertValues(KoalaEvent.FACEBOOK_CONFIRM, KoalaEvent.USER_SIGNUP,
      KoalaEvent.SIGNUP_NEWSLETTER_TOGGLE, KoalaEvent.SIGNUP_NEWSLETTER_TOGGLE);
  }
}
