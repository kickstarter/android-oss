package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventName
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.viewmodels.projectpage.LatePledgeCheckoutViewModel
import org.junit.Test

class LatePledgeCheckoutViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: LatePledgeCheckoutViewModel

    private fun setUpEnvironment(environment: Environment) {
        viewModel = LatePledgeCheckoutViewModel.Factory(environment).create(LatePledgeCheckoutViewModel::class.java)
    }

    @Test
    fun `test send PageViewed event`() {
        setUpEnvironment(environment())

        val rw = RewardFactory.rewardWithShipping()
        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val addOns = listOf(rw, rw, rw)
        val rule = ShippingRuleFactory.germanyShippingRule()
        val shipAmount = 3.0
        val totalAmount = 300.0
        val bonusAmount = 5.0

        val projectData = ProjectDataFactory.project(project = project)

        viewModel.userRewardSelection(rw)
        viewModel.sendPageViewedEvent(
            projectData,
            addOns,
            rule,
            shipAmount,
            totalAmount,
            bonusAmount
        )

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun `test send CTAClicked event`() {
        setUpEnvironment(environment())

        val rw = RewardFactory.rewardWithShipping()
        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val addOns = listOf(rw, rw, rw)
        val rule = ShippingRuleFactory.germanyShippingRule()
        val shipAmount = 3.0
        val totalAmount = 300.0
        val bonusAmount = 5.0

        val projectData = ProjectDataFactory.project(project = project)
        viewModel.userRewardSelection(rw)
        viewModel.sendSubmitCTAEvent(
            projectData,
            addOns,
            rule,
            shipAmount,
            totalAmount,
            bonusAmount
        )

        this.segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }
}
