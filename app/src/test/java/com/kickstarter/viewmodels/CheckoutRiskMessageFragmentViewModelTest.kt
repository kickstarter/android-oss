package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.UrlUtils
import org.junit.Test
import rx.observers.TestSubscriber

class CheckoutRiskMessageFragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: CheckoutRiskMessageFragmentViewModel.ViewModel

    private val openLearnMoreAboutAccountabilityLink = TestSubscriber<String>()
    private fun setUpEnvironment(environment: Environment) {
        this.vm = CheckoutRiskMessageFragmentViewModel.ViewModel(environment)

        this.vm.outputs.openLearnMoreAboutAccountabilityLink().subscribe(this.openLearnMoreAboutAccountabilityLink)
    }

    @Test
    fun testOnLearnMoreAboutAccountabilityLinkClicked() {
        setUpEnvironment(environment())
        this.vm.inputs.onLearnMoreAboutAccountabilityLinkClicked()
        this.openLearnMoreAboutAccountabilityLink.assertValue(
            UrlUtils
                .appendPath(environment().webEndpoint(), CheckoutRiskMessageFragmentViewModel.TRUST)
        )
    }
}
