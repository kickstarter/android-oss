package com.kickstarter.libs.utils

import android.util.Pair
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.KSCurrency
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.LocationFactory
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
        val caRule = ShippingRuleFactory.canadaShippingRule().toBuilder()
            .estimatedMin(3.0)
            .estimatedMax(30.0)
            .build()

        val shippingPreference = Reward.ShippingPreference.UNRESTRICTED
        val reward = RewardFactory.reward().toBuilder()
            .shippingPreference(shippingPreference.name)
            .shippingType(shippingPreference.name)
            .shippingPreferenceType(shippingPreference)
            .shippingRules(listOf(mxRule, usRule))
            .build()
        val rewards = listOf(reward)

        val selectedShippingRule = caRule

        val estimatedShippingString = RewardViewUtils.getEstimatedShippingRange(
            context, ksCurrency, ksString, project, rewards, selectedShippingRule,
            multipleQuantitiesAllowed = false,
            useUserPreference = false,
            useAbout = false
        )

        assertEquals("$2-$20", estimatedShippingString)
    }

    @Test
    fun `test estimated shipping range for reward with restricted shipping`() {
        val context = context()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val project = ProjectFactory.project()

        val usRule = ShippingRuleFactory.usShippingRule().toBuilder()
            .estimatedMin(1.0)
            .estimatedMax(10.0)
            .build()
        val mxRule = ShippingRuleFactory.mexicoShippingRule().toBuilder()
            .estimatedMin(2.0)
            .estimatedMax(20.0)
            .build()

        val shippingPreference = Reward.ShippingPreference.RESTRICTED

        val reward = RewardFactory.reward().toBuilder()
            .shippingPreferenceType(shippingPreference)
            .shippingPreference(shippingPreference.name.lowercase())
            .shippingType(shippingPreference.name.lowercase())
            .shippingRules(listOf(mxRule, usRule))
            .build()
        val rewards = listOf(reward)

        val selectedShippingRule = usRule

        val estimatedShippingString = RewardViewUtils.getEstimatedShippingRange(
            context, ksCurrency, ksString, project, rewards, selectedShippingRule,
            multipleQuantitiesAllowed = false,
            useUserPreference = false,
            useAbout = false,
        )

        assertEquals("$1-$10", estimatedShippingString)
    }

    @Test
    fun `test estimated shipping range for digital reward`() {
        val context = context()

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val project = ProjectFactory.project()

        val shippingPreference = Reward.ShippingPreference.NONE

        val reward = RewardFactory.reward().toBuilder()
            .shippingPreferenceType(shippingPreference)
            .shippingPreference(shippingPreference.name.lowercase())
            .shippingType(shippingPreference.name.lowercase())
            .shippingRules(listOf())
            .build()
        val rewards = listOf(reward)

        val selectedShippingRule = ShippingRuleFactory.usShippingRule()

        val estimatedShippingString = RewardViewUtils.getEstimatedShippingRange(
            context, ksCurrency, ksString, project, rewards, selectedShippingRule,
            multipleQuantitiesAllowed = false,
            useUserPreference = false,
            useAbout = false,
        )

        assertTrue(estimatedShippingString.isEmpty())
    }

    @Test
    fun `test estimated shipping range when min or max is 0, when min and max are 0`() {
        val context = context()

        val configForUSUser = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(configForUSUser)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val caProject = ProjectFactory.caProject()

        val usRule = ShippingRuleFactory.usShippingRule().toBuilder()
            .estimatedMin(0.0)
            .estimatedMax(0.0)
            .build()
        val mxRule = ShippingRuleFactory.mexicoShippingRule().toBuilder()
            .estimatedMin(0.0)
            .estimatedMax(20.0)
            .build()

        val shippingPreference = Reward.ShippingPreference.RESTRICTED
        val reward = RewardFactory.reward().toBuilder()
            .shippingPreference(shippingPreference.name)
            .shippingType(shippingPreference.name)
            .shippingPreferenceType(shippingPreference)
            .shippingRules(listOf(mxRule, usRule))
            .build()
        val rewards = listOf(reward)

        val estimatedShippingStringForUS = RewardViewUtils.getEstimatedShippingRange(
            context, ksCurrency, ksString, caProject, rewards, usRule,
            multipleQuantitiesAllowed = false,
            useUserPreference = false,
            useAbout = false,
        )
        val estimatedShippingStringForMX = RewardViewUtils.getEstimatedShippingRange(
            context, ksCurrency, ksString, caProject, rewards, mxRule,
            multipleQuantitiesAllowed = false,
            useUserPreference = false,
            useAbout = false,
        )

        assertEquals("", estimatedShippingStringForUS)
        assertEquals("CA$ 0-CA$ 20", estimatedShippingStringForMX)
    }

    @Test
    fun `getEstimatedShippingCost - returns null when reward is not shippable`() {
        val context = context()

        val configForUSUser = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(configForUSUser)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val caProject = ProjectFactory.caProject()

        val shippingPreference = Reward.ShippingPreference.NONE
        val digitalReward = RewardFactory.reward().toBuilder()
            .shippingPreferenceType(shippingPreference)
            .shippingPreference(shippingPreference.name.lowercase())
            .shippingType(shippingPreference.name.lowercase())
            // For testing. In practice, the shipping rules list for digital rewards is typically empty.
            .shippingRules(listOf(ShippingRuleFactory.usShippingRule()))
            .build()

        val selectedLocation = LocationFactory.unitedStates()

        val estimatedShippingString = RewardViewUtils.getEstimatedShippingCost(
            context, ksCurrency, ksString, caProject, digitalReward, selectedLocation.id(),
        )

        assertNull(estimatedShippingString)
    }

    @Test
    fun `getEstimatedShippingCost - returns null when no shipping rule matches and reward shipping is restricted `() {
        val context = context()

        val configForUSUser = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(configForUSUser)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val caProject = ProjectFactory.caProject()

        val usRule = ShippingRuleFactory.usShippingRule().toBuilder()
            .estimatedMin(1.0)
            .estimatedMax(10.0)
            .build()
        val mxRule = ShippingRuleFactory.mexicoShippingRule().toBuilder()
            .estimatedMin(2.0)
            .estimatedMax(20.0)
            .build()

        val shippingPreference = Reward.ShippingPreference.RESTRICTED
        val reward = RewardFactory.reward().toBuilder()
            .shippingPreferenceType(shippingPreference)
            .shippingPreference(shippingPreference.name.lowercase())
            .shippingType(shippingPreference.name.lowercase())
            .shippingRules(listOf(mxRule, usRule))
            .build()

        val selectedLocation = LocationFactory.canada()

        val estimatedShippingString = RewardViewUtils.getEstimatedShippingCost(
            context, ksCurrency, ksString, caProject, reward, selectedLocation.id(),
        )

        assertNull(estimatedShippingString)
    }

    @Test
    fun `getEstimatedShippingCost - uses first shipping rule by default when none match but reward ships worldwide `() {
        val context = context()

        val configForUSUser = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(configForUSUser)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val caProject = ProjectFactory.caProject()

        val usRule = ShippingRuleFactory.usShippingRule().toBuilder()
            .estimatedMin(1.0)
            .estimatedMax(10.0)
            .build()

        val mxLocation = LocationFactory.mexico()
        val mxRule = ShippingRuleFactory.mexicoShippingRule().toBuilder()
            .location(mxLocation)
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

        val caLocation = LocationFactory.canada()

        val selectedLocation = caLocation

        val expectedEstimatedShippingString = RewardViewUtils.getEstimatedShippingCost(
            context, ksCurrency, ksString, caProject, reward, mxLocation.id()
        )
        val estimatedShippingString = RewardViewUtils.getEstimatedShippingCost(
            context, ksCurrency, ksString, caProject, reward, selectedLocation.id(),
        )

        assertEquals(expectedEstimatedShippingString, estimatedShippingString)
    }

    @Test
    fun `getEstimatedShippingCost - returns formatted range when min or max is non-zero`() {
        val context = context()

        val configForUSUser = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(configForUSUser)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val caProject = ProjectFactory.caProject()

        val usLocation = LocationFactory.unitedStates()
        val usRule = ShippingRuleFactory.usShippingRule().toBuilder()
            .location(usLocation)
            .estimatedMin(0.0)
            .estimatedMax(10.0)
            .build()
        val mxLocation = LocationFactory.mexico()
        val mxRule = ShippingRuleFactory.mexicoShippingRule().toBuilder()
            .location(mxLocation)
            .estimatedMin(2.0)
            .estimatedMax(20.0)
            .build()

        val shippingPreference = Reward.ShippingPreference.RESTRICTED
        val reward = RewardFactory.reward().toBuilder()
            .shippingPreference(shippingPreference.name)
            .shippingType(shippingPreference.name)
            .shippingPreferenceType(shippingPreference)
            .shippingRules(listOf(mxRule, usRule))
            .build()

        val estimatedShippingStringForUS = RewardViewUtils.getEstimatedShippingCost(
            context, ksCurrency, ksString, caProject, reward, usLocation.id(),
        )
        val estimatedShippingStringForMX = RewardViewUtils.getEstimatedShippingCost(
            context, ksCurrency, ksString, caProject, reward, mxLocation.id(),
        )

        assertEquals("About CA$ 0-CA$ 10", estimatedShippingStringForUS)
        assertEquals("About CA$ 2-CA$ 20", estimatedShippingStringForMX)
    }

    @Test
    fun `getEstimatedShippingCost - returns formatted cost when min and max are 0, cost is positive`() {
        val context = context()

        val configForUSUser = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(configForUSUser)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val caProject = ProjectFactory.caProject()

        val usLocation = LocationFactory.unitedStates()
        val usRule = ShippingRuleFactory.usShippingRule().toBuilder()
            .location(usLocation)
            .estimatedMin(0.0)
            .estimatedMax(0.0)
            .cost(10.0)
            .build()

        val shippingPreference = Reward.ShippingPreference.RESTRICTED
        val reward = RewardFactory.reward().toBuilder()
            .shippingPreference(shippingPreference.name)
            .shippingType(shippingPreference.name)
            .shippingPreferenceType(shippingPreference)
            .shippingRules(listOf(usRule))
            .build()

        val estimatedShippingString = RewardViewUtils.getEstimatedShippingCost(
            context, ksCurrency, ksString, caProject, reward, usLocation.id(),
        )

        assertEquals("CA$ 10", estimatedShippingString)
    }

    @Test
    fun `getEstimatedShippingCost - returns empty string when min, max, and cost are 0`() {
        val context = context()

        val configForUSUser = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(configForUSUser)
        val ksCurrency = KSCurrency(currentConfig)

        val ksString = ksString()
        val caProject = ProjectFactory.caProject()

        val usLocation = LocationFactory.unitedStates()
        val usRule = ShippingRuleFactory.usShippingRule().toBuilder()
            .location(usLocation)
            .estimatedMin(0.0)
            .estimatedMax(0.0)
            .cost(0.0)
            .build()

        val shippingPreference = Reward.ShippingPreference.RESTRICTED
        val reward = RewardFactory.reward().toBuilder()
            .shippingPreference(shippingPreference.name)
            .shippingType(shippingPreference.name)
            .shippingPreferenceType(shippingPreference)
            .shippingRules(listOf(usRule))
            .build()

        val estimatedShippingString = RewardViewUtils.getEstimatedShippingCost(
            context, ksCurrency, ksString, caProject, reward, usLocation.id(),
        )

        assertEquals("", estimatedShippingString)
    }
}
