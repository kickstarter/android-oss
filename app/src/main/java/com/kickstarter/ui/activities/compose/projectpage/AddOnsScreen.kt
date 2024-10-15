package com.kickstarter.ui.activities.compose.projectpage

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.getCurrencySymbols
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.compose.designsystem.KSCircularProgressIndicator
import com.kickstarter.ui.compose.designsystem.KSPrimaryGreenButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typography
import com.kickstarter.ui.views.compose.checkout.BonusSupportContainer
import java.math.RoundingMode

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun AddOnsScreenPreview() {
    KSTheme {
        Scaffold(
            backgroundColor = colors.backgroundAccentGraySubtle
        ) { padding ->
            AddOnsScreen(
                modifier = Modifier
                    .padding(padding)
                    .systemBarsPadding(),
                environment = Environment.Builder().build(),
                lazyColumnListState = rememberLazyListState(),
                selectedReward = RewardFactory.reward().toBuilder().minimum(5.0).build(),
                addOns = (0..10).map {
                    Reward.builder()
                        .title("Item Number $it")
                        .description("This is a description for item $it")
                        .id(it.toLong())
                        .quantity(3)
                        .convertedMinimum((100 * (it + 1)).toDouble())
                        .isAvailable(it != 0)
                        .limit(if (it == 0) 1 else 10)
                        .build()
                },
                project =
                Project.builder()
                    .currency("USD")
                    .currentCurrency("USD")
                    .build(),
                onItemAddedOrRemoved = { q, l -> },
                bonusAmountChanged = {},
                onContinueClicked = {},
                addOnCount = 2,
                totalPledgeAmount = 30.0,
                totalBonusSupport = 5.0
            )
        }
    }
}

@Composable
fun AddOnsScreen(
    modifier: Modifier = Modifier,
    environment: Environment,
    lazyColumnListState: LazyListState,
    selectedReward: Reward,
    addOns: List<Reward>,
    project: Project,
    onItemAddedOrRemoved: (quantityForId: Int, rewardId: Long) -> Unit,
    bonusAmountChanged: (amount: Double) -> Unit,
    isLoading: Boolean = false,
    currentShippingRule: ShippingRule = ShippingRule.builder().build(),
    onContinueClicked: () -> Unit,
    addOnCount: Int = 0,
    totalPledgeAmount: Double,
    totalBonusSupport: Double
) {
    val context = LocalContext.current
    val currencySymbolStartAndEnd = environment.ksCurrency()?.getCurrencySymbols(project)
    val totalAmountString = environment.ksCurrency()?.let {
        RewardViewUtils.styleCurrency(
            value = totalPledgeAmount,
            project = project,
            ksCurrency = it
        ).toString()
    } ?: ""

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                Column {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(
                            topStart = dimensions.radiusLarge,
                            topEnd = dimensions.radiusLarge
                        ),
                        color = colors.backgroundSurfacePrimary,
                        elevation = dimensions.elevationLarge,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensions.paddingMediumLarge)
                        ) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(id = R.string.Total_amount),
                                        style = typography.subheadlineMedium,
                                        color = colors.textPrimary
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    Text(
                                        text = totalAmountString,
                                        style = typography.subheadlineMedium,
                                        color = colors.textPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(dimensions.paddingSmall))

                                KSPrimaryGreenButton(
                                    onClickAction = onContinueClicked,
                                    text =
                                    if (addOnCount == 0)
                                        stringResource(id = R.string.Continue)
                                    else {
                                        when {
                                            addOnCount == 1 -> environment.ksString()?.format(
                                                stringResource(R.string.Continue_with_quantity_count_add_ons_one),
                                                "quantity_count",
                                                addOnCount.toString()
                                            ) ?: ""

                                            addOnCount > 1 -> environment.ksString()?.format(
                                                stringResource(R.string.Continue_with_quantity_count_add_ons_many),
                                                "quantity_count",
                                                addOnCount.toString()
                                            ) ?: ""

                                            else -> stringResource(id = R.string.Continue)
                                        }
                                    },
                                    isEnabled = true
                                )
                            }
                        }
                    }
                }
            },
            backgroundColor = colors.backgroundAccentGraySubtle
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(
                        start = dimensions.paddingMedium,
                        end = dimensions.paddingMedium,
                        top = dimensions.paddingMedium
                    )
                    .padding(paddingValues = padding),
                state = lazyColumnListState
            ) {
                item {
                    if (addOns.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.Customize_your_reward_with_optional_addons),
                            style = typography.title3Bold,
                            color = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                    val initAmount = if (project.isBacking())
                        project.backing()?.bonusAmount() ?: 0.0
                    else if (RewardUtils.isNoReward(selectedReward))
                        RewardUtils.minPledgeAmount(selectedReward, project)
                    else 0.0

                    BonusSupportContainer(
                        selectedReward = selectedReward,
                        initialAmount = initAmount,
                        maxAmount = RewardUtils.maxPledgeAmount(selectedReward, project),
                        minPledge = RewardUtils.minPledgeAmount(selectedReward, project),
                        totalAmount = totalPledgeAmount,
                        totalBonusSupport = totalBonusSupport,
                        currencySymbolAtStart = currencySymbolStartAndEnd?.first,
                        currencySymbolAtEnd = currencySymbolStartAndEnd?.second,
                        onBonusSupportPlusClicked = bonusAmountChanged,
                        onBonusSupportMinusClicked = bonusAmountChanged,
                        onBonusSupportInputted = bonusAmountChanged,
                        environment = environment
                    )
                }

                items(
                    items = addOns
                ) { reward ->

                    Spacer(modifier = Modifier.height(dimensions.paddingMedium))

                    AddOnsContainer(
                        rewardId = reward.id(),
                        title = reward.title() ?: "",
                        amount = environment.ksCurrency()?.format(
                            reward.minimum(),
                            project,
                            true,
                        ) ?: "",
                        conversionAmount = if (project.currentCurrency() == project.currency()) "" else {
                            environment.ksString()?.format(
                                stringResource(R.string.About_reward_amount),
                                "reward_amount",
                                environment.ksCurrency()?.format(
                                    reward.convertedMinimum(),
                                    project,
                                    true,
                                    RoundingMode.HALF_UP,
                                    true
                                )
                            )
                        },
                        shippingAmount = RewardViewUtils.getAddOnShippingAmountString(
                            context = context,
                            project = project,
                            reward = reward,
                            rewardShippingRules = reward.shippingRules(),
                            ksCurrency = environment.ksCurrency(),
                            ksString = environment.ksString(),
                            selectedShippingRule = currentShippingRule
                        ),
                        description = reward.description() ?: "",
                        includesList = reward.addOnsItems()?.map {
                            environment.ksString()?.format(
                                "rewards_info_item_quantity_title", it.quantity(),
                                "quantity", it.quantity().toString(),
                                "title", it.item().name()
                            ) ?: ""
                        } ?: listOf(),
                        limit = reward.limit() ?: -1,
                        buttonEnabled = reward.isAvailable(),
                        buttonText = stringResource(id = R.string.Add),
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
                                        multipleQuantitiesAllowed = (reward.limit() ?: -1) > 1,
                                        useUserPreference = false,
                                        useAbout = true
                                    )
                                }
                            }
                        } else null,
                        onItemAddedOrRemoved = { quantityForId, rwId ->
                            onItemAddedOrRemoved(quantityForId, rwId)
                        },
                        quantity = reward.quantity() ?: 0
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(dimensions.paddingDoubleLarge))
                }
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundAccentGraySubtle.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            KSCircularProgressIndicator()
        }
    }
}
