package com.kickstarter.libs.utils

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import org.junit.Test

class RewardViewUtilsTest : KSRobolectricTestCase() {

    @Test
    fun testPledgeButtonText() {

        // - Selected reward with addOns if choosing same reward always available unless project ended
        val project = ProjectFactory.backedProjectWithRewardAndAddOnsLimitReached()
        val rw = project.backing()?.reward() ?: RewardFactory.noReward()
        assertEquals(R.string.Continue, RewardViewUtils.pledgeButtonText(project, rw))

        assertEquals(R.string.Select, RewardViewUtils.pledgeButtonText(ProjectFactory.project(), RewardFactory.reward()))
        assertEquals(R.string.No_longer_available, RewardViewUtils.pledgeButtonText(ProjectFactory.project(), RewardFactory.ended()))
        assertEquals(R.string.No_longer_available, RewardViewUtils.pledgeButtonText(ProjectFactory.project(), RewardFactory.limitReached()))
        val backedProject = ProjectFactory.backedProject()
        val backedReward = backedProject.backing()?.reward() ?: RewardFactory.reward()
        assertEquals(R.string.Selected, RewardViewUtils.pledgeButtonText(backedProject, backedReward))
        assertEquals(R.string.Select, RewardViewUtils.pledgeButtonText(backedProject, RewardFactory.reward()))
        val backedSuccessfulProject = ProjectFactory.backedProject().toBuilder().state(Project.STATE_SUCCESSFUL).build()
        val backedSuccessfulReward = backedSuccessfulProject.backing()?.reward() ?: RewardFactory.reward()
        assertEquals(R.string.Selected, RewardViewUtils.pledgeButtonText(backedSuccessfulProject, backedSuccessfulReward))
    }

    /**
     * Given a backedProject the backed reward has available addOns
     * when not backed addOns
     * Then the text for the button should be R.string.Continue
     */
    @Test
    fun rewardBackedHasAvailableAddOns_whenNotBackedAddOns_textContinue() {
        val backedProjectRwHasAvailableNotBackedAddOns = ProjectFactory.backedProjectRewardAvailableAddOnsNotBackedAddOns()
        val backedRwAvailableAddOns = requireNotNull(backedProjectRwHasAvailableNotBackedAddOns.backing()?.reward())
        assertEquals(R.string.Continue, RewardViewUtils.pledgeButtonText(backedProjectRwHasAvailableNotBackedAddOns, backedRwAvailableAddOns))
    }

    /**
     * Given a backedProject the backed reward
     * has available addOns
     * when not backed addOns
     * when reward is not available
     * Then the text for the button should be R.string.Continue
     */
    @Test
    fun rewardBackedHasAvailableAddOns_whenNotBackedAddOnsAndRewardUnavailable_textContinue() {
        val backedProjectRwHasAvailableNotBackedAddOns = ProjectFactory.backedProjectWithRewardLimitReached()
        val backedRwAvailableAddOns = requireNotNull(backedProjectRwHasAvailableNotBackedAddOns.backing()?.reward())
        assertEquals(R.string.Continue, RewardViewUtils.pledgeButtonText(backedProjectRwHasAvailableNotBackedAddOns, backedRwAvailableAddOns))
    }

    @Test
    fun testShippingSummary() {
        val ksString = ksString()
        assertEquals("Ships worldwide", RewardViewUtils.shippingSummary(context(), ksString, Pair(R.string.Ships_worldwide, null)))
        assertEquals("Limited shipping", RewardViewUtils.shippingSummary(context(), ksString, Pair(R.string.Limited_shipping, null)))
        assertEquals("Limited shipping", RewardViewUtils.shippingSummary(context(), ksString, Pair(R.string.location_name_only, null)))
        assertEquals("Nigeria only", RewardViewUtils.shippingSummary(context(), ksString, Pair(R.string.location_name_only, "Nigeria")))
    }

    @Test
    fun `test when user exceeds max pledge amount the appropriate error message is returned`() {
        val maxPledgeAmount = 1000.0
        val totalAmount = 1100.0
        val totalBonusSupport = 600.0

        val maxInputString = RewardViewUtils.getMaxInputString(
            context(),
            RewardFactory.reward(),
            maxPledgeAmount,
            totalAmount,
            totalBonusSupport,
            kotlin.Pair<String?, String?>("$", null),
            environment().toBuilder().build()
        )
        assertEquals("Enter an amount less than $500.", maxInputString)
    }

    @Test
    fun `test estimated shipping range for reward with worldwide shipping uses first shipping rule by default`() {
        val context = context()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val project = ProjectFactory.project() // KSCurrency.formatWithUserPreference()

        val usRule = ShippingRuleFactory.usShippingRule().toBuilder()
            .estimatedMin(1.0)
            .estimatedMax(10.0)
            .build()
        val mxRule = ShippingRuleFactory.mexicoShippingRule().toBuilder()
            .estimatedMin(2.0)
            .estimatedMax(20.0)
            .build()

        val shippingPreference = Reward.ShippingPreference.UNRESTRICTED
        val reward = RewardFactory.reward().toBuilder()
            .shippingPreference(shippingPreference.name)
            .shippingType(shippingPreference.name)
            .shippingPreferenceType(shippingPreference)
            .shippingRules(listOf(mxRule, usRule))
            .build()
        val rewards = listOf(reward)

        val selectedShippingRule = usRule

        val estimatedShippingString = RewardViewUtils.getEstimatedShippingCostString(
            context, ksCurrency, ksString, project, rewards, selectedShippingRule,
            multipleQuantitiesAllowed = false,
            useUserPreference = false,
            useAbout = false
        )

        assertEquals("$2-$20", estimatedShippingString)
    }

    @Test
    fun `test estimated shipping range for reward with worldwide shipping uses first selected shipping rule when forced`() {
        val context = context()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val project = ProjectFactory.project() // KSCurrency.formatWithUserPreference()

        val usRule = ShippingRuleFactory.usShippingRule().toBuilder()
            .estimatedMin(1.0)
            .estimatedMax(10.0)
            .build()
        val mxRule = ShippingRuleFactory.mexicoShippingRule().toBuilder()
            .estimatedMin(2.0)
            .estimatedMax(20.0)
            .build()

        val shippingPreference = Reward.ShippingPreference.UNRESTRICTED
        val reward = RewardFactory.reward().toBuilder()
            .shippingPreference(shippingPreference.name)
            .shippingType(shippingPreference.name)
            .shippingPreferenceType(shippingPreference)
            .shippingRules(listOf(mxRule, usRule))
            .build()
        val rewards = listOf(reward)

        val selectedShippingRule = usRule

        val estimatedShippingString = RewardViewUtils.getEstimatedShippingCostString(
            context, ksCurrency, ksString, project, rewards, selectedShippingRule,
            multipleQuantitiesAllowed = false,
            useUserPreference = false,
            useAbout = false,
            forceSelectedShippingRule = true
        )

        assertEquals("$1-$10", estimatedShippingString)
    }
}
