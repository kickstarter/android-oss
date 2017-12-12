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

public class FacebookConfimationViewModelTest extends KSRobolectricTestCase {
  private FacebookConfirmationViewModel.ViewModel vm;
  private final TestSubscriber<String> prefillEmail = new TestSubscriber<>();
  private final TestSubscriber<String> signupError = new TestSubscriber<>();
  private final TestSubscriber<Void> signupSuccess = new TestSubscriber<>();
  private final TestSubscriber<Boolean> sendNewslettersIsChecked = new TestSubscriber<>();

  @Test
  public void testPrefillEmail() {
    ErrorEnvelope.FacebookUser facebookUser = ErrorEnvelope.FacebookUser.builder()
      .id(1).name("Test").email("test@kickstarter.com")
      .build();

    this.vm = new FacebookConfirmationViewModel.ViewModel(environment());
    this.vm.intent(new Intent().putExtra(IntentKey.FACEBOOK_USER, facebookUser));

    this.vm.outputs.prefillEmail().subscribe(this.prefillEmail);

    this.prefillEmail.assertValue("test@kickstarter.com");
  }

  @Test
  public void testSignupErrorDisplay() {
    final ApiClientType apiClient = new MockApiClient() {
      @NonNull
      @Override
      public Observable<AccessTokenEnvelope> registerWithFacebook(@NonNull String fbAccessToken, boolean sendNewsletters) {
        return Observable.error(ApiExceptionFactory.apiError(ErrorEnvelope.builder().ksrCode("oh no").build()));
      }
    };

    final Environment environment = environment().toBuilder().apiClient(apiClient).build();
    this.vm = new FacebookConfirmationViewModel.ViewModel(environment);

    this.vm.outputs.signupError().subscribe(this.signupError);

    this.vm.inputs.createNewAccountClick();
    this.signupError.assertValue("oh no");
  }

  @Test
  public void testToggleSendNewsLetter() {

  }
}
