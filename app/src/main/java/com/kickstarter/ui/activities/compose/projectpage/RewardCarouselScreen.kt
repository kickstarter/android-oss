package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.isAllowedToPledge
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNullOrZero
import com.kickstarter.mock.factories.RewardsItemFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.KSRewardCard
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.views.compose.checkout.ShippingSelector
import org.joda.time.DateTime
import java.math.RoundingMode

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RewardCarouselScreenPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = KSTheme.colors.backgroundAccentGraySubtle
        ) { padding ->
            RewardCarouselScreen(
                modifier = Modifier
                    .padding(padding)
                    .systemBarsPadding(),
                lazyRowState = rememberLazyListState(),
                environment = Environment.Builder().build(),
                rewards = (0..10).map {
                    Reward.builder()
                        .title("Item Number $it")
                        .description("This is a description for item $it")
                        .id(it.toLong())
                        .minimum(20.0)
                        .backersCount(2)
                        .estimatedDeliveryOn(DateTime.now())
                        .convertedMinimum((100 * (it + 1)).toDouble())
                        .isAvailable(it != 0)
                        .hasAddons(true)
                        .limit(if (it == 0) 1 else 10)
                        .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS)
                        .rewardsItems(
                            listOf(
                                RewardsItemFactory.rewardsItem(),
                                RewardsItemFactory.rewardsItem2(),
                                RewardsItemFactory.rewardsItem3()
                            )
                        )
                        .build()
                },
                project =
                Project.builder()
                    .currency("USD")
                    .currentCurrency("USD")
                    .state(Project.STATE_LIVE)
                    .build(),
                onRewardSelected = {},
                currentShippingRule = ShippingRuleFactory.usShippingRule(),
                countryList = listOf(
                    ShippingRuleFactory.usShippingRule(),
                    ShippingRuleFactory.germanyShippingRule()
                ),
                onShippingRuleSelected = {}
            )
        }
    }
}

@Composable
fun RewardCarouselScreen(
    modifier: Modifier = Modifier,
    lazyRowState: LazyListState,
    environment: Environment,
    rewards: List<Reward>,
    project: Project,
    backing: Backing? = null,
    isLoading: Boolean = false,
    onRewardSelected: (reward: Reward) -> Unit,
    countryList: List<ShippingRule> = emptyList<ShippingRule>(),
    onShippingRuleSelected: (ShippingRule) -> Unit = {},
    currentShippingRule: ShippingRule = ShippingRule.builder().build()
) {
    val context = LocalContext.current
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Scaffold(
        modifier = modifier,
        backgroundColor = KSTheme.colors.backgroundAccentGraySubtle,
        bottomBar = {
            Column {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom = KSTheme.dimensions.paddingMediumSmall,
                            top = KSTheme.dimensions.paddingMediumSmall
                        ),
                    text = environment.ksString()?.let {
                        it.format(
                            "Rewards_count_rewards", rewards.size,
                            "rewards_count", NumberUtils.format(rewards.size)
                        )
                    } ?: "",
                    color = KSTheme.colors.kds_support_400,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { padding ->
        Column {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KSTheme.colors.backgroundAccentGraySubtle.copy(alpha = 0.5f))
                        .clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    KSCircularProgressIndicator()
                }
            }

            if (countryList.isNotEmpty()) {
                ShippingSelector(
                    modifier = Modifier
                        .padding(dimensions.paddingMedium),
                    interactionSource = interactionSource,
                    currentShippingRule = currentShippingRule,
                    countryList = countryList,
                    onShippingRuleSelected = onShippingRuleSelected
                )
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(paddingValues = padding),
                state = lazyRowState,
                contentPadding =
                PaddingValues(
                    start = KSTheme.dimensions.paddingMedium,
                    end = KSTheme.dimensions.paddingMedium,
                    top = KSTheme.dimensions.paddingMedium
                ),
                horizontalArrangement = Arrangement.spacedBy(KSTheme.dimensions.paddingMediumLarge)
            ) {

                items(
                    items = rewards,
                ) { reward ->

                    val ctaButtonEnabled = when {
                        RewardUtils.isNoReward(reward) -> true
                        !reward.hasAddons() && backing?.isBacked(reward) != true -> true
                        backing?.rewardId() != reward.id() && RewardUtils.isAvailable(
                            project,
                            reward
                        ) && reward.isAvailable() -> true

                        reward.hasAddons() && backing?.rewardId() == reward.id() && (project.isLive || (project.postCampaignPledgingEnabled() ?: false && project.isInPostCampaignPledgingPhase() ?: false)) && reward.isAvailable() -> true

                        else -> false
                    }
                    val isBacked = backing?.isBacked(reward) ?: false

                    val ctaButtonText = when {
                        ctaButtonEnabled -> R.string.Select
                        else -> R.string.No_longer_available
                    }

                    val remaining = reward.remaining() ?: -1

                    if (RewardUtils.isNoReward(reward)) {
                        KSRewardCard(
                            isCTAButtonEnabled = ctaButtonEnabled,
                            ctaButtonText = stringResource(id = ctaButtonText),
                            title = if (isBacked) stringResource(id = R.string.You_pledged_without_a_reward) else stringResource(
                                id = R.string.Pledge_without_a_reward
                            ),
                            description = if (isBacked) stringResource(id = R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality) else stringResource(
                                id = R.string.Back_it_because_you_believe_in_it
                            ),
                            onRewardSelectClicked = { onRewardSelected(reward) },
                            isCTAButtonVisible = project.isAllowedToPledge(),
                            yourSelectionIsVisible = project.backing()?.isBacked(reward) ?: false,
                        )
                    } else {
                        KSRewardCard(
                            onRewardSelectClicked = { onRewardSelected(reward) },
                            amount = environment.ksCurrency()?.let {
                                RewardViewUtils.styleCurrency(
                                    reward.minimum(),
                                    project,
                                    it
                                ).toString()
                            },
                            conversion = if (project.currentCurrency() == project.currency()) "" else {
                                val conversionAmount = environment.ksCurrency()?.format(
                                    reward.convertedMinimum(),
                                    project,
                                    true,
                                    RoundingMode.HALF_UP,
                                    true
                                )
                                environment.ksString()?.format(
                                    stringResource(id = R.string.About_reward_amount),
                                    "reward_amount",
                                    conversionAmount
                                )
                            },
                            description = reward.description(),
                            title = reward.title(),
                            backerCountBadgeText =
                            if (reward.backersCount().isNullOrZero()) ""
                            else {
                                environment.ksString()?.let {
                                    it.format(
                                        "rewards_info_backer_count_backers",
                                        requireNotNull(reward.backersCount()),
                                        "backer_count",
                                        NumberUtils.format(requireNotNull(reward.backersCount()))
                                    )
                                }
                            },
                            isCTAButtonEnabled = ctaButtonEnabled,
                            includes = if (RewardUtils.isItemized(reward) && !reward.rewardsItems()
                                .isNullOrEmpty() && environment.ksString().isNotNull()
                            ) {
                                reward.rewardsItems()?.map { rewardItems ->
                                    environment.ksString()?.format(
                                        "rewards_info_item_quantity_title", rewardItems.quantity(),
                                        "quantity", rewardItems.quantity().toString(),
                                        "title", rewardItems.item().name()
                                    ) ?: ""
                                } ?: emptyList()
                            } else {
                                emptyList()
                            },

                            estimatedDelivery = if (reward.estimatedDeliveryOn().isNotNull()) {
                                DateTimeUtils.estimatedDeliveryOn(requireNotNull(reward.estimatedDeliveryOn()))
                            } else "",
                            yourSelectionIsVisible = project.backing()?.isBacked(reward) ?: false,
                            localPickup = if (RewardUtils.isLocalPickup(reward) && !RewardUtils.isShippable(
                                    reward
                                )
                            ) {
                                reward.localReceiptLocation()?.displayableName() ?: ""
                            } else {
                                ""
                            },
                            ctaButtonText = stringResource(id = ctaButtonText),
                            expirationDateText =
                            environment.ksString()?.let {
                                if (RewardUtils.deadlineCountdownValue(reward) <= 0) ""
                                else "" + RewardUtils.deadlineCountdownValue(reward) + " " + RewardUtils.deadlineCountdownDetail(
                                    reward,
                                    context,
                                    it
                                )
                            },
                            shippingSummaryText =
                            environment.ksString()?.let { ksString ->
                                if (RewardUtils.isShippable(reward)) {
                                    RewardUtils.shippingSummary(reward)?.let {
                                        RewardViewUtils.shippingSummary(
                                            context = context,
                                            ksString = ksString,
                                            it
                                        )
                                    }
                                } else {
                                    ""
                                }
                            },
                            remainingText =
                            environment.ksString()?.let { ksString ->
                                if (!reward.isLimited()) {
                                    if (remaining > 0) {
                                        ksString.format(
                                            stringResource(id = R.string.Left_count_left_few),
                                            "left_count",
                                            NumberUtils.format(remaining)
                                        )
                                    } else ""
                                } else ""
                            },
                            estimatedShippingCost =
                            if (!RewardUtils.isDigital(reward) && RewardUtils.isShippable(reward) && !RewardUtils.isLocalPickup(reward)) {
                                environment.ksCurrency()?.let { ksCurrency ->
                                    environment.ksString()?.let { ksString ->
                                        RewardViewUtils.getEstimatedShippingCostString(
                                            context = context,
                                            ksCurrency = ksCurrency,
                                            ksString = ksString,
                                            project = project,
                                            rewards = listOf(reward),
                                            selectedShippingRule = currentShippingRule,
                                            multipleQuantitiesAllowed = false,
                                            useUserPreference = false,
                                            useAbout = true
                                        )
                                    }
                                }
                            } else null,
                            addonsPillVisible = reward.hasAddons(),
                            isCTAButtonVisible = project.isAllowedToPledge()
                        )
                    }
                }
            }
        }
    }
}
