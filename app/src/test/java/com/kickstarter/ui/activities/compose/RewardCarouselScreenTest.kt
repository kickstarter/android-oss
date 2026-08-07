package com.kickstarter.ui.activities.compose.projectpage

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kickstarter.mock.factories.BackingFactory
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

        val reward = RewardFactory.reward().toBuilder().id(1L).isAvailable(true).build()
        val rewardEndingSoon = RewardFactory.endingSoon().toBuilder().id(2L).isAvailable(true).build()
        val rewardExpired = RewardFactory.ended().toBuilder().id(4L).isAvailable(false).build()

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
                        and hasText(context.getString(R.string.No_longer_available))
                        and isNotEnabled()
                )
            )
        }
    }

    @Test
    fun `test reward selectability given an existing backing`() {
        val context = context()

        val noReward = RewardFactory.noReward()
        val reward = RewardFactory.reward().toBuilder().id(1L).hasAddons(true).isAvailable(true).build()
        val rewardEndingSoon = RewardFactory.endingSoon().toBuilder().id(2L).hasAddons(true).isAvailable(true).build()
        val rewardExpiredExplicit = RewardFactory.ended().toBuilder().id(3L).hasAddons(true).isAvailable(false).build()
        val rewardLimitReached = RewardFactory.limitReached().toBuilder().id(4L).hasAddons(true).isAvailable(false).build()

        val rewards = listOf(
            noReward, reward, rewardEndingSoon, rewardExpiredExplicit, rewardLimitReached
        )

        val backingState = mutableStateOf(BackingFactory.backing(reward))

        composeTestRule.setContent {
            val project = remember(backingState.value) {
                Project.builder().state(Project.STATE_LIVE).backing(backingState.value).build()
            }

            KSTheme {
                RewardCarouselScreen(
                    lazyRowState = rememberLazyListState(),
                    environment = environment(),
                    rewards = rewards,
                    project = project,
                    backing = project.backing(),
                    onRewardSelected = {},
                    currentShippingRule = ShippingRuleFactory.usShippingRule(),
                    countryList = listOf(ShippingRuleFactory.usShippingRule()),
                    onShippingRuleSelected = {}
                )
            }
        }

        with(rewardCarousel) {
            var targetReward = noReward
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.Select))
                        and isEnabled()
                )
            )

            targetReward = reward
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.Select))
                        and isEnabled()
                ) and hasAnyDescendant(hasText(context.getString(R.string.Your_selection)))
            )

            targetReward = rewardExpiredExplicit
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.No_longer_available))
                        and isNotEnabled()
                )
            )

            targetReward = rewardLimitReached
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.No_longer_available))
                        and isNotEnabled()
                )
            )
        }

        backingState.value = BackingFactory.backing(rewardExpiredExplicit)

        with(rewardCarousel) {
            var targetReward = noReward
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.Select))
                        and isEnabled()
                )
            )

            targetReward = reward
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.Select))
                        and isEnabled()
                )
            )

            targetReward = rewardExpiredExplicit
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.Select))
                        and isEnabled()
                ) and hasAnyDescendant(hasText(context.getString(R.string.Your_selection)))
            )

            targetReward = rewardLimitReached
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.No_longer_available))
                        and isNotEnabled()
                )
            )
        }

        backingState.value = BackingFactory.backing(rewardLimitReached)

        with(rewardCarousel) {
            var targetReward = noReward
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.Select))
                        and isEnabled()
                )
            )

            targetReward = reward
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.Select))
                        and isEnabled()
                )
            )

            targetReward = rewardExpiredExplicit
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.No_longer_available))
                        and isNotEnabled()
                )
            )

            targetReward = rewardLimitReached
            performScrollToIndex(rewards.indexOf(targetReward))
            composeTestRule.onNodeWithTag(
                RewardCarouselTestTag.REWARD_CARD.name + targetReward.id()
            ).assert(
                hasAnyDescendant(
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
                        and hasText(context.getString(R.string.Select))
                        and isEnabled()
                ) and hasAnyDescendant(hasText(context.getString(R.string.Your_selection)))
            )
        }
    }
}
