package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.CurrentConfigTypeV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.ConfigFactory.config
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.services.ApiClientTypeV2
import com.kickstarter.services.apiresponses.AccessTokenEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.FacebookUser
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class FacebookConfimationViewModelTest : KSRobolectricTestCase() {
    private val prefillEmail = TestSubscriber<String>()
    private val signupError = TestSubscriber<String>()
    private val signupSuccess = TestSubscriber<Unit>()
    private val sendNewslettersIsChecked = TestSubscriber<Boolean>()
    private val disposables = CompositeDisposable()

    @Test
    fun testPrefillEmail() {
        val facebookUser = FacebookUser.builder()
            .id(1L).name("Test").email("test@kickstarter.com")
            .build()

        val vm = FacebookConfirmationViewModel.FacebookConfirmationViewModel(environment())
        vm.provideIntent(Intent().putExtra(IntentKey.FACEBOOK_USER, facebookUser))

        vm.outputs.prefillEmail().subscribe { this.prefillEmail.onNext(it) }.addToDisposable(disposables)

        prefillEmail.assertValue("test@kickstarter.com")
    }

    @Test
    fun testSignupErrorDisplay() {
        val apiClient: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun registerWithFacebook(
                fbAccessToken: String,
                sendNewsletters: Boolean
            ): Observable<AccessTokenEnvelope> {
                return Observable.error(
                    ApiExceptionFactory.apiError(
                        ErrorEnvelope.builder().httpCode(404).errorMessages(listOf("oh no")).build()
                    )
                )
            }
        }

        val environment = environment().toBuilder().apiClientV2(apiClient).build()
        val vm = FacebookConfirmationViewModel.FacebookConfirmationViewModel(environment)

        vm.outputs.signupError().subscribe { this.signupError.onNext(it) }.addToDisposable(disposables)

        vm.provideIntent(Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "token"))
        vm.inputs.sendNewslettersClick(true)
        vm.inputs.createNewAccountClick()

        signupError.assertValue("oh no")
    }

    @Test
    fun testSuccessfulUserCreation() {
        val apiClient: ApiClientTypeV2 = MockApiClientV2()

        val environment = environment().toBuilder().apiClientV2(apiClient).build()
        val vm = FacebookConfirmationViewModel.FacebookConfirmationViewModel(environment)

        vm.outputs.signupSuccess().subscribe { this.signupSuccess.onNext(it) }.addToDisposable(disposables)

        vm.provideIntent(Intent().putExtra(IntentKey.FACEBOOK_TOKEN, "token"))
        vm.inputs.sendNewslettersClick(true)
        vm.inputs.createNewAccountClick()

        signupSuccess.assertValueCount(1)
    }

    @Test
    fun testToggleSendNewsLetter_isNotChecked() {
        val currentConfig: CurrentConfigTypeV2 = MockCurrentConfigV2()
        currentConfig.config(config().toBuilder().countryCode("US").build())
        val environment = environment().toBuilder().currentConfig2(currentConfig).build()
        val vm = FacebookConfirmationViewModel.FacebookConfirmationViewModel(environment)

        vm.outputs.sendNewslettersIsChecked().subscribe { this.sendNewslettersIsChecked.onNext(it) }.addToDisposable(disposables)
        sendNewslettersIsChecked.assertValue(false)

        vm.inputs.sendNewslettersClick(true)
        vm.inputs.sendNewslettersClick(false)

        sendNewslettersIsChecked.assertValues(false, true, false)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
