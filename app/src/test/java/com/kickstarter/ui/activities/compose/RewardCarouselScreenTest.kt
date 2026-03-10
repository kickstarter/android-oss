package com.kickstarter.ui.activities.compose.projectpage

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.RewardsItemFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.compose.designsystem.KSTheme
import org.joda.time.DateTime
import org.junit.Test

class RewardCarouselScreenTest : KSRobolectricTestCase() {
    private val shippingSelector = composeTestRule.onNodeWithTag(RewardCarouselTestTag.SHIPPING_SELECTOR.name)

    private val rewardCarousel = composeTestRule.onNodeWithTag(RewardCarouselTestTag.REWARD_CAROUSEL.name)

    @Test
    fun `RewardsCarouselScreen displays RewardCards and ShippingSelector`() {
        val rewards = List(3) {
            Reward.builder()
                .id(it.toLong())
                .title("Reward $it")
                .description("Description $it")
                .minimum(10.0)
                .convertedMinimum(100.0)
                .estimatedDeliveryOn(DateTime.parse("2020-01-01T00:00:00.000Z"))
                .isAvailable(true)
                .hasAddons(false)
                .limit(10)
                .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS)
                .rewardsItems(listOf(RewardsItemFactory.rewardsItem()))
                .build()
        }

        val project = Project.builder()
            .currency("USD")
            .currentCurrency("USD")
            .state(Project.STATE_LIVE)
            .build()

        val shippingRules = listOf(
            ShippingRuleFactory.usShippingRule(),
            ShippingRuleFactory.germanyShippingRule()
        )

        composeTestRule.setContent {
            KSTheme {
                RewardCarouselScreen(
                    lazyRowState = rememberLazyListState(),
                    environment = com.kickstarter.libs.Environment.Builder().build(),
                    rewards = rewards,
                    project = project,
                    onRewardSelected = {},
                    currentShippingRule = shippingRules.first(),
                    countryList = shippingRules,
                    onShippingRuleSelected = {}
                )
            }
        }
        shippingSelector.assertIsDisplayed()

        rewardCarousel.assertIsDisplayed()

        for (reward in rewards) {
            rewardCarousel.performScrollToIndex(rewards.indexOf(reward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + reward.id()
            ).assertIsDisplayed()
        }
    }

    @Test
    fun `Test rewardUtils for isNoReward`() {
        val rewards = listOf(
            Reward.builder().id(0L).title("No Reward").build(),
            Reward.builder().id(123L).title("Secret Reward").isSecretReward(true).isAvailable(true).build(),
            Reward.builder().id(12L).title("Normal Reward").isSecretReward(false).isAvailable(true).build()
        )

        val project = Project.builder().state(Project.STATE_LIVE).build()

        composeTestRule.setContent {
            KSTheme {
                RewardCarouselScreen(
                    lazyRowState = rememberLazyListState(),
                    environment = com.kickstarter.libs.Environment.Builder().build(),
                    rewards = rewards,
                    project = project,
                    onRewardSelected = {},
                    currentShippingRule = ShippingRuleFactory.usShippingRule(),
                    countryList = listOf(ShippingRuleFactory.usShippingRule()),
                    onShippingRuleSelected = {}
                )
            }
        }

        // Assertions for RewardUtils
        assertTrue(RewardUtils.isNoReward(rewards[0]))
        assertFalse(RewardUtils.isNoReward(rewards[1]))
        assertFalse(RewardUtils.isNoReward(rewards[2]))
    }

    @Test
    fun `test expired rewards are not selectable`() {
        val context = context()

        val reward = RewardFactory.reward().toBuilder().id(0L).build()
        val rewardEndingSoon = RewardFactory.endingSoon().toBuilder().id(1L).build()
        val rewardExpired = RewardFactory.ended().toBuilder().id(2L).build()

        val rewards = listOf(
            reward, rewardEndingSoon, rewardExpired
        )

        val project = Project.builder().state(Project.STATE_LIVE).build()

        composeTestRule.setContent {
            KSTheme {
                RewardCarouselScreen(
                    lazyRowState = rememberLazyListState(),
                    environment = com.kickstarter.libs.Environment.Builder().build(),
                    rewards = rewards,
                    project = project,
                    onRewardSelected = {},
                    currentShippingRule = ShippingRuleFactory.usShippingRule(),
                    countryList = listOf(ShippingRuleFactory.usShippingRule()),
                    onShippingRuleSelected = {}
                )
            }
        }

        with(rewardCarousel) {
            performScrollToIndex(rewards.indexOf(reward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + reward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.Select))
                        and isEnabled()
                )
            )

            performScrollToIndex(rewards.indexOf(rewardExpired))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + rewardExpired.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context().getString(R.string.No_longer_available))
                        and isNotEnabled()
                )
            )
        }
    }
}
