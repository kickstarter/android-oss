package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNullOrZero
import com.kickstarter.mock.factories.RewardsItemFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.compose.KSRewardCard
import com.kickstarter.ui.compose.designsystem.KSTheme
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
                modifier = Modifier.padding(padding),
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
                onRewardSelected = {}
            )
        }
    }
}

@Composable
fun RewardCarouselScreen(
    modifier: Modifier,
    lazyRowState: LazyListState,
    environment: Environment,
    rewards: List<Reward>,
    project: Project,
    onRewardSelected: (reward: Reward) -> Unit
) {
    val context = LocalContext.current

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
                            "Rewards_count_rewards", project.rewards()?.size ?: 0,
                            "rewards_count", NumberUtils.format(project.rewards()?.size ?: 0)
                        )
                    } ?: "",
                    color = KSTheme.colors.kds_support_400,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { padding ->
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
                    !reward.hasAddons() && project.backing()?.isBacked(reward) != true -> true
                    project.backing()?.rewardId() != reward.id() && RewardUtils.isAvailable(
                        project,
                        reward
                    ) -> true

                    reward.hasAddons() && project.backing()
                        ?.rewardId() == reward.id() && (project.isLive || (project.postCampaignPledgingEnabled() ?: false && project.isInPostCampaignPledgingPhase() ?: false)) -> true

                    else -> false
                }
                val isBacked = project.backing()?.isBacked(reward) ?: false
                val ctaButtonText = RewardViewUtils.pledgeButtonText(project, reward)

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
                        onRewardSelectClicked = { onRewardSelected(reward) }
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
                        conversion = environment.ksCurrency()?.let {
                            it.format(
                                reward.convertedMinimum(),
                                project,
                                true,
                                RoundingMode.HALF_UP,
                                true
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
                        addonsPillVisible = reward.hasAddons()
                    )
                }
            }
        }
    }
}
