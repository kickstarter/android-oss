package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.EMAIL_VERIFICATION_FLOW
import com.kickstarter.libs.utils.extensions.EMAIL_VERIFICATION_SKIP
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class EmailVerificationInterstitialFragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: EmailVerificationInterstitialFragmentViewModel.ViewModel
    private val startEmailActivity = TestSubscriber.create<Void>()
    private val isSkipLinkShown = TestSubscriber.create<Boolean>()
    private val dismissInterstitial = TestSubscriber.create<Void>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = EmailVerificationInterstitialFragmentViewModel.ViewModel(environment)
        this.vm.outputs.startEmailActivity().subscribe(startEmailActivity)
        this.vm.outputs.isSkipLinkShown().subscribe(isSkipLinkShown)
        this.vm.outputs.dismissInterstitial().subscribe(dismissInterstitial)
    }

    @Test
    fun init_whenOpenEmailInboxPressedEmits_shouldEmitToStartEmailActivityStream() {
        setUpEnvironment(environment())

        this.vm.inputs.openInboxButtonPressed()
        this.startEmailActivity.assertValue(null)
    }

    @Test
    fun isSkipLinkShown_whenFeatureFlagActive_shouldBeShown() {

        val mockConfig = MockCurrentConfig()
        mockConfig.config(ConfigFactory.configWithFeaturesEnabled(mapOf(
                Pair(EMAIL_VERIFICATION_SKIP, true)
        )))

        val environment = environment().toBuilder()
                .currentConfig(mockConfig)
                .build()

        setUpEnvironment(environment)

        this.isSkipLinkShown.assertValue(true)
    }

    @Test
    fun isSkipLinkShown_whenFeatureFlagNotActive_shouldBeShown() {

        val mockConfig = MockCurrentConfig()
        mockConfig.config(ConfigFactory.configWithFeaturesEnabled(mapOf(
                Pair(EMAIL_VERIFICATION_SKIP, second = false)
        )))

        val environment = environment().toBuilder()
                .currentConfig(mockConfig)
                .build()

        setUpEnvironment(environment)

        this.isSkipLinkShown.assertNoValues()
    }

    @Test
    fun dismissInterstitial_whenSkipButtonPressed_dismissInterstitial() {
        setUpEnvironment(environment())

        this.vm.inputs.dismissInterstitial()
        this.dismissInterstitial.assertValueCount(1)
    }
}
